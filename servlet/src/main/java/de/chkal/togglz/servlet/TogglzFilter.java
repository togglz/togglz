package de.chkal.togglz.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.chkal.togglz.core.context.FeatureContext;
import de.chkal.togglz.core.manager.FeatureManager;
import de.chkal.togglz.core.manager.FeatureManagerFactory;
import de.chkal.togglz.servlet.ui.AdminUserInterface;

public class TogglzFilter implements Filter {

    private final Logger log = LoggerFactory.getLogger(TogglzFilter.class);

    private ServletContext servletContext;

    private AdminUserInterface featureAdminPage;

    public void init(FilterConfig filterConfig) throws ServletException {
        servletContext = filterConfig.getServletContext();

        FeatureManager featureManager = new FeatureManagerFactory().build(servletContext);

        FeatureContext.bindFeatureManager(featureManager);

        servletContext.setAttribute(FeatureManager.class.getName(), featureManager);

        featureAdminPage = new AdminUserInterface(featureManager, filterConfig.getServletContext(), "togglz");

        log.info("FeatureFilter started!");

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (!featureAdminPage.process(httpRequest, httpResponse)) {
            chain.doFilter(request, response);
        }

    }

    public void destroy() {
        FeatureContext.unbindFeatureManager();
    }

}
