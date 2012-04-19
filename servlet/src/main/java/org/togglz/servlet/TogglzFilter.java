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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerFactory;
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

    private final Logger log = LoggerFactory.getLogger(TogglzFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {

        ServletContext servletContext = filterConfig.getServletContext();

        // create FeatureManager
        FeatureManager featureManager = new FeatureManagerFactory().build(servletContext);
        WebAppFeatureManagerProvider.bindFeatureManager(featureManager);

        log.info("FeatureFilter started!");

    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
            ServletException {

        HttpServletRequest request = (HttpServletRequest) req;

        try {

            // store the request in a thread local
            HttpServletRequestHolder.set(request);

            // continue processing the chain
            chain.doFilter(req, resp);

        } finally {
            // remove the request from the thread local
            HttpServletRequestHolder.release();
        }

    }

    public void destroy() {
        WebAppFeatureManagerProvider.unbindFeatureManager();
    }

}
