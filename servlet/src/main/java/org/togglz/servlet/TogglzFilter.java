package org.togglz.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.url.URLStateRepository;
import org.togglz.servlet.spi.WebAppFeatureManagerProvider;
import org.togglz.servlet.util.HttpServletRequestHolder;

/**
 * 
 * This filter is the central component of the Togglz Servlet integration module. It is responsible to bootstrap the
 * {@link FeatureManager} and register it with {@link WebAppFeatureManagerProvider}.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class TogglzFilter implements Filter {

    private final Log log = LogFactory.getLog(TogglzFilter.class);
    private static final String URL_PARAMETER = "togglz";

    public void init(FilterConfig filterConfig) throws ServletException {

        ServletContext servletContext = filterConfig.getServletContext();

        // create FeatureManager if required
        if (isCreateLocalFeatureManager(servletContext)) {
            FeatureManager featureManager = new FeatureManagerBuilder().autoDiscovery(servletContext).build();
            WebAppFeatureManagerProvider.bind(featureManager);
        }

        log.info("TogglzFilter started!");

    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
            ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        String featureString = request.getParameter(URL_PARAMETER);
        if (featureString != null && !featureString.isEmpty()) {
            URLStateRepository.getInstance().setFeatureState(featureString);
        }

        try {

            // store the request in a thread local
            HttpServletRequestHolder.bind(request);

            // continue processing the chain
            chain.doFilter(req, resp);

        } finally {
            // remove the request from the thread local
            HttpServletRequestHolder.release();
        }

    }

    public void destroy() {
        WebAppFeatureManagerProvider.release();
    }

    /**
     * Returns <code>true</code> if the filter should create a local {@link FeatureManager} for the application.
     */
    private static boolean isCreateLocalFeatureManager(ServletContext servletContext) {

        String value = servletContext.getInitParameter("org.togglz.LOCAL_FEATURE_MANAGER");

        // "false" only if explicitly configured this way
        if (value != null && "false".equalsIgnoreCase(value.trim())) {
            return false;
        }

        // the default case
        return true;
    }

}
