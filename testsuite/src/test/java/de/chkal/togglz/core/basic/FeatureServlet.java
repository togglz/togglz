package de.chkal.togglz.core.basic;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.chkal.togglz.core.Feature;

@WebServlet(urlPatterns = "/features")
public class FeatureServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        StringBuilder builder = new StringBuilder();

        for (Feature f : BasicFeatures.values()) {
            builder.append(f.name() + " = " + f.isEnabled() + "\n");
        }

        resp.getOutputStream().write(builder.toString().getBytes());

    }

}
