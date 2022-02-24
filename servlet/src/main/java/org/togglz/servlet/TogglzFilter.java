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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Togglz;
import org.togglz.core.bootstrap.FeatureManagerBootstrapper;
import org.togglz.core.context.ContextClassLoaderFeatureManagerProvider;
import org.togglz.core.context.FeatureContext;
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

    public static final String EXECUTED = TogglzFilter.class.getName() + ".done";

    private final Logger log = LoggerFactory.getLogger(TogglzFilter.class);

    private FeatureManager bootstrappedFeatureManager;

    private CompositeRequestListener requestListener;

    public void init(FilterConfig filterConfig) throws ServletException {

        // build the configuration object
        TogglzFilterConfig config = new TogglzFilterConfig(filterConfig.getServletContext());

        requestListener = new CompositeRequestListener(Services.getSorted(RequestListener.class));

        // did the user specify whether to perform bootstrap or not?
        Boolean bootstrap = config.isPerformBootstrap();

        // try to autodetect whether bootstrapping is required
        if (bootstrap == null) {

            FeatureManager existingFeatureManager = FeatureContext.getFeatureManagerOrNull();

            if (existingFeatureManager == null) {
                log.debug("Could not find any existing FeatureManager");
                bootstrap = true;
            } else {
                log.debug("Found existing FeatureManager: " + existingFeatureManager.getName());
                bootstrap = false;
            }

        }

        // run bootstrap process
        if (bootstrap) {

            log.debug("Starting FeatureManager bootstrap process");

            FeatureManagerBootstrapper boostrapper = new FeatureManagerBootstrapper();
            bootstrappedFeatureManager = boostrapper.createFeatureManager(filterConfig.getServletContext());

            for (FeatureManagerListener listener : Services.getSorted(FeatureManagerListener.class)) {
                listener.start(bootstrappedFeatureManager);
            }

            ContextClassLoaderFeatureManagerProvider.bind(bootstrappedFeatureManager);

            log.debug("FeatureManager has been created: " + bootstrappedFeatureManager.getName());

        }

        log.info(Togglz.getNameWithVersion() + " started");

    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
        throws IOException, ServletException {

        if (Boolean.TRUE.equals(req.getAttribute(EXECUTED))) {

            chain.doFilter(req, resp);

        } else {

            req.setAttribute(EXECUTED, Boolean.TRUE);

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

    }

    public void destroy() {

        // release only if the filter created it
        if (bootstrappedFeatureManager != null) {

            // notify listeners about the shutdown
            for (FeatureManagerListener listener : Services.getSorted(FeatureManagerListener.class)) {
                listener.stop(bootstrappedFeatureManager);
            }

            // release
            ContextClassLoaderFeatureManagerProvider.release();

        }

    }

}
