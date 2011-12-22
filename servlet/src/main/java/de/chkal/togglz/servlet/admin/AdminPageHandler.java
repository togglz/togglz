package de.chkal.togglz.servlet.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.chkal.togglz.core.manager.FeatureManager;
import de.chkal.togglz.servlet.admin.pages.FeatureListPage;
import de.chkal.togglz.servlet.admin.pages.RedirectPage;

public class AdminPageHandler {

    private List<AdminPage> pages = new ArrayList<AdminPage>();

    private String prefix;

    public AdminPageHandler(FeatureManager featureManager, ServletContext servletContext, String dir) {

        // example: /myapp/togglez
        this.prefix = servletContext.getContextPath() + "/" + dir;

        // register pages
        this.pages.add(new RedirectPage());
        this.pages.add(new FeatureListPage(featureManager));
    }

    public boolean process(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (request.getRequestURI().startsWith(prefix)) {

            String path = request.getRequestURI().substring(prefix.length());

            for (AdminPage page : pages) {

                if (page.handles(path)) {
                    page.process(request, response);
                    return true;
                }

            }
        }

        return false;

    }

}
