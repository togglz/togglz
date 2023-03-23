package org.togglz.test;

import java.io.IOException;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;

@WebServlet(urlPatterns = "/features")
public class FeatureServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FeatureManager featureManager = FeatureContext.getFeatureManager();
        StringBuilder builder = new StringBuilder();
        for (Feature f : featureManager.getFeatures()) {
            builder.append(f.name()).append(" = ").append(featureManager.isActive(f)).append("\n");
        }
        resp.getOutputStream().write(builder.toString().getBytes());
    }

}
