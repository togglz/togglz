package org.togglz.shiro.test;

import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

@WebServlet(urlPatterns = "/logout")
public class ShiroLogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();

        if (!subject.isAuthenticated()) {
            response.getOutputStream().print("SUCCESS");
        }
        else {
            response.getOutputStream().print("FAILED");
        }
        response.getOutputStream().flush();
    }
}
