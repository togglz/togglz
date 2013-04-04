package org.togglz.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.togglz.core.Togglz;
import org.togglz.core.bootstrap.FeatureManagerBootstrapper;
import org.togglz.core.context.ContextClassLoaderFeatureManagerProvider;
import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.spi.FeatureManagerListener;
import org.togglz.core.util.Services;
import org.togglz.servlet.spi.RequestListener;

/**
 * 
 * This filter is the central component of the Togglz Servlet integration module. It is responsible to bootstrap the
 * {@link FeatureManager} and register it with {@link ContextClassLoaderFeatureManagerProvider}.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class TogglzFilter implements Filter {

    private final Log log = LogFactory.getLog(TogglzFilter.class);

    private TogglzFilterConfig config;

    private FeatureManager featureManager;

    private CompositeRequestListener requestListener;

    public void init(FilterConfig filterConfig) throws ServletException {

        // build the configuration object
        config = new TogglzFilterConfig(filterConfig.getServletContext());

        requestListener = new CompositeRequestListener(Services.getSorted(RequestListener.class));

        // create FeatureManager if required
        if (config.isCreateLocalFeatureManager()) {

            FeatureManagerBootstrapper boostrapper = new FeatureManagerBootstrapper();
            featureManager = boostrapper.createFeatureManager(filterConfig.getServletContext());

            for (FeatureManagerListener listener : Services.getSorted(FeatureManagerListener.class)) {
                listener.start(featureManager);
            }

            ContextClassLoaderFeatureManagerProvider.bind(featureManager);

        }

        log.info(Togglz.getNameWithVersion() + " started");

    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
        throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        try {

            // notify listeners
            requestListener.begin(request, response);

            // continue processing the chain
            chain.doFilter(req, resp);

        } finally {

            // notify listeners
            requestListener.end(request, response);

        }

    }

    public void destroy() {

        // release only if the filter created it
        if (featureManager != null) {

            // notify listeners about the shutdown
            for (FeatureManagerListener listener : Services.getSorted(FeatureManagerListener.class)) {
                listener.stop(featureManager);
            }

            // release
            ContextClassLoaderFeatureManagerProvider.release();

        }

    }

}
