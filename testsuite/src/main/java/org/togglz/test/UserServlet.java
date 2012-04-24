package org.togglz.test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.user.FeatureUser;

@WebServlet(urlPatterns = "/user")
public class UserServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        FeatureManager featureManager = FeatureContext.getFeatureManager();
        FeatureUser user = featureManager.getCurrentFeatureUser();

        StringBuilder builder = new StringBuilder();
        builder.append("USER = " + (user != null ? user.getName() : "null"));
        builder.append("ADMIN = " + (user != null ? user.isFeatureAdmin() : "null"));

        resp.getOutputStream().write(builder.toString().getBytes());

    }

}
