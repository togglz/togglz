package org.togglz.rest.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.LazyResolvingFeatureManager;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.util.Strings;
import org.togglz.rest.api.model.FeatureToggleRepresentation;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

/**
 * Exposes a REST API for enabling/disabling Feature Toggles.
 *
 * All services consumes and produces "application/json".
 * The supported methods are:
 *
 * <br/>
 * <p>
 * <b>Content negotiation</b> is performed through the "Accept" header so that
 * user agents can specify the desired format. Supported content types are: json and xml
 *</p>
 * <br/>
 * <table>
 *     <tr>
 *         <th>Description</th>
 *         <th>Verb</th>
 *         <th>Response codes</th>
 *     </tr>
 *     <tr>
 *         <td>Lists all feature toggles</td>
 *         <td>GET</td>
 *         <td>200</td>
 *     </tr>
 *     <tr>
 *         <td>Gets a specific feature toggle (by name)</td>
 *         <td>GET</td>
 *         <td>200, 404</td>
 *     </tr>
 *     <tr>
 *         <td>Updates a specific feature toggle (by name)</td>
 *         <td>PUT</td>
 *         <td>200, 404</td>
 *     </tr>
 * </table>
 *
 * <br/>
 *
 * <pre>
 *
 * &lt;servlet&gt;&lt;/servlet&gt
 *
 * GET basepath/
 *
 * GET basepath/featureName
 *
 * PUT basepath/featureName
 * </pre>
 *
 * @author Fábio Franco Uechi
 */
public class TogglzRestApiServlet extends HttpServlet {

    private static final String FORWARD_SLASH = "/";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ACCEPT = "Accept";

    protected ServletContext servletContext;
    protected FeatureManager featureManager;
    protected Map<String, RequestHandler> registry;

    @Override
    public void init(ServletConfig config) throws ServletException {
        featureManager = new LazyResolvingFeatureManager();
        servletContext = config.getServletContext();
        registry = ImmutableMap.<String, RequestHandler>of(
            JsonRequestHandler.APPLICATION_JSON, new JsonRequestHandler(),
            XmlRequestHandler.APPLICATION_XML, new XmlRequestHandler()
            );
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String prefix = req.getContextPath() + req.getServletPath();
        String path = req.getRequestURI().substring(prefix.length());
        if( Strings.isBlank(path) || FORWARD_SLASH.equals(path) ) {
            ok(req, resp, features());
        } else {
            getFeature(req, resp, path);
        }
    }

    private void getFeature(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException, ServletException {
        String featureName = path.startsWith(FORWARD_SLASH) ? path.substring(1) : path;
        FeatureToggleRepresentation feature = feature(featureName);
        if (feature != null) {
            ok(req, resp, feature);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void ok(HttpServletRequest request, HttpServletResponse resp, Object obj) throws IOException, ServletException {
        Optional<RequestHandler> handler = handler(request, ACCEPT);
        if(handler.isPresent()) {
            resp.getWriter().write(handler.get().serialize(obj));
            resp.addHeader(CONTENT_TYPE, handler.get().contentType());
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<RequestHandler> handler = handler(req, CONTENT_TYPE);
        if(handler.isPresent()) {
            FeatureToggleRepresentation ft = handler.get().deserialize(req.getReader());
            for(Feature f: featureManager.getFeatures()) {
                if( f.name().equals(ft.getName())) {
                    FeatureState state = featureManager.getFeatureState(f);
                    Boolean enabled = ft.getEnabled();
                    state.setEnabled(enabled);

                    if (ft.hasActivationStrategy()) {
                        String strategyId = checkRegistered(ft.getStrategyId());

                        if (Strings.isBlank(strategyId)) {
                            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown strategy id: " + ft.getStrategyId());
                            return;
                        }

                        state.setStrategyId(strategyId);
                        for(String paramName: ft.getParameterNames()) {
                            state.setParameter(paramName, ft.getParameter(paramName));
                        }
                    }

                    featureManager.setFeatureState(state);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    return;
                }
            }
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, String.format("Supported media types are: %s", registry.keySet()));
        }
    }

    private String checkRegistered(String strategyId) {
        for (ActivationStrategy activationStrategy : featureManager.getActivationStrategies()) {
            if (activationStrategy.getId().equals(strategyId))  {
                return strategyId;
            }
        }
        return null;
    }

    private Optional<RequestHandler> handler(HttpServletRequest req, String headerName) throws ServletException {
        String contentType = req.getHeader(headerName);
        RequestHandler handler = registry.get(contentType) ;
        return Optional.fromNullable(handler);
    }

    private FeatureToggleRepresentation feature(String featureName) {
        for(Feature f: featureManager.getFeatures()) {
            if( f.name().equals(featureName)) {
                FeatureState state = featureManager.getFeatureState(f);
                return feature(state);
            }
        }
        return null;
    }

    private List<FeatureToggleRepresentation> features() {
        List<FeatureToggleRepresentation> features = new ArrayList<FeatureToggleRepresentation>();
        for(Feature f: featureManager.getFeatures()) {
            FeatureState state = featureManager.getFeatureState(f);
            FeatureToggleRepresentation obj = feature(state);
            features.add(obj);
        }
        return features;
    }

    private FeatureToggleRepresentation feature(FeatureState featureState) {
        return FeatureToggleRepresentation.of(featureState);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        notAllowed(resp);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        notAllowed(resp);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        notAllowed(resp);
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        notAllowed(resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        notAllowed(resp);
    }

    private void notAllowed(HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}

