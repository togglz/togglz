package org.togglz.seam.security.test;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.picketlink.idm.impl.api.PasswordCredential;
import org.togglz.core.util.Strings;

@WebServlet(urlPatterns = "/login")
public class SeamSecurityLoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private Credentials credentials;

    @Inject
    private Identity identity;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String user = request.getParameter("user");
        if (Strings.isBlank(user)) {
            throw new IllegalArgumentException("Missing 'user' parameter!");
        }

        credentials.setUsername(user);
        credentials.setCredential(new PasswordCredential("secret"));

        identity.login();

        if (identity.isLoggedIn()) {
            response.getOutputStream().print("SUCCESS");
        }
        else {
            response.getOutputStream().print("FAILED");
        }

        response.getOutputStream().flush();

    }

}
