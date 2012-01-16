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
import de.chkal.togglz.core.user.FeatureUser;
import de.chkal.togglz.core.util.Strings;
import de.chkal.togglz.servlet.ui.AdminUserInterface;
import de.chkal.togglz.servlet.util.HttpServletRequestHolder;

public class TogglzFilter implements Filter {

    private final Logger log = LoggerFactory.getLogger(TogglzFilter.class);

    private AdminUserInterface featureAdminPage;

    public void init(FilterConfig filterConfig) throws ServletException {

        ServletContext servletContext = filterConfig.getServletContext();

        // create FeatureManager
        FeatureManager featureManager = new FeatureManagerFactory().build(servletContext);
        FeatureContext.bindFeatureManager(featureManager);

        // enable admin interface
        String adminInterfaceEnabled = servletContext.getInitParameter("de.chkal.togglz.ENABLE_ADMIN_UI");
        if (Strings.equalsIgnoreCase(adminInterfaceEnabled, "true")) {

            // custom prefix for the admin UI
            String prefix = servletContext.getInitParameter("de.chkal.togglz.ADMIN_UI_PATH");
            if (Strings.isBlank(prefix)) {
                prefix = "togglz";
            }

            featureAdminPage = new AdminUserInterface(featureManager, filterConfig.getServletContext(), prefix);

        }

        log.info("FeatureFilter started!");

    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
            ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        try {

            // store the request in a thread local
            HttpServletRequestHolder.set(request);

            // try to process request if admin UI is enabled
            boolean processedByAdminUi = false;
            if (featureAdminPage != null) {

                // try to get the current FeatureUser
                FeatureManager featureManager = FeatureContext.getFeatureManager();
                FeatureUser user = featureManager.getCurrentFeatureUser();

                // only authorized users are allowed to access the admin pages
                if (user != null && user.isFeatureAdmin()) {
                    processedByAdminUi = featureAdminPage.process(request, response);
                }

            }

            // process chain
            if (!processedByAdminUi) {
                chain.doFilter(req, resp);
            }

        } finally {
            // remove the request from the thread local
            HttpServletRequestHolder.set(null);
        }

    }

    public void destroy() {
        FeatureContext.unbindFeatureManager();
    }

}
