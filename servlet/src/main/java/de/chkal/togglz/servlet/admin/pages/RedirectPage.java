package de.chkal.togglz.servlet.admin.pages;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.chkal.togglz.servlet.admin.AdminPage;

public class RedirectPage extends AdminPage {

    @Override
    public boolean handles(String path) {
        return "".equals(path) || "/".equals(path);
    }

    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException {

        StringBuilder url = new StringBuilder();
        url.append(request.getRequestURI());
        if (!url.toString().endsWith("/")) {
            url.append("/");
        }
        url.append("index");
        response.sendRedirect(url.toString());
    }

}
