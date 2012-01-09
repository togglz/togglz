package de.chkal.togglz.test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.context.FeatureContext;
import de.chkal.togglz.core.manager.FeatureManager;


@WebServlet(urlPatterns = "/features")
public class FeatureServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        FeatureManager featureManager = FeatureContext.getFeatureManager();
        
        StringBuilder builder = new StringBuilder();

        for (Feature f : featureManager.getFeatures()) {
            builder.append(f.name() + " = " + featureManager.isActive(f) + "\n");
        }

        resp.getOutputStream().write(builder.toString().getBytes());

    }

}
