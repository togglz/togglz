package org.togglz.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.togglz.core.context.ContextClassLoaderFeatureManagerProvider;
import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.servlet.util.HttpServletRequestHolder;

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

    public void init(FilterConfig filterConfig) throws ServletException {

        // build the configuration object
        config = new TogglzFilterConfig(filterConfig.getServletContext());

        // create FeatureManager if required
        if (config.isCreateLocalFeatureManager()) {

            FeatureManager featureManager = new FeatureManagerBuilder()
                .autoDiscovery(filterConfig.getServletContext())
                .build();

            ContextClassLoaderFeatureManagerProvider.bind(featureManager);

        }

        log.info("TogglzFilter started!");

    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
        throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;

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

        // releae only if the filter created it
        if (config.isCreateLocalFeatureManager()) {
            ContextClassLoaderFeatureManagerProvider.release();
        }

    }

}
