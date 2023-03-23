package org.togglz.test;

import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.user.FeatureUser;

@WebServlet(urlPatterns = "/user")
public class UserServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        FeatureManager featureManager = FeatureContext.getFeatureManager();
        FeatureUser user = featureManager.getCurrentFeatureUser();

        String builder = "USER = " + (user != null ? user.getName() : "null") +
                "ADMIN = " + (user != null ? user.isFeatureAdmin() : "null");
        resp.getOutputStream().write(builder.getBytes());
    }
}
