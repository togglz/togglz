package org.togglz.seam.security.test;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.security.Identity;

@WebServlet(urlPatterns = "/logout")
public class SeamSecurityLogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private Identity identity;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        identity.logout();
        response.getOutputStream().print("SUCCESS");
        response.getOutputStream().flush();

    }

}
