package de.chkal.togglz.test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.chkal.togglz.test.basic.BasicFeatures;


@WebServlet(urlPatterns = "/features")
public class FeatureServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        StringBuilder builder = new StringBuilder();

        for (BasicFeatures f : BasicFeatures.values()) {
            builder.append(f.name() + " = " + f.isActive() + "\n");
        }

        resp.getOutputStream().write(builder.toString().getBytes());

    }

}
