package org.togglz.shiro.test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

@WebServlet(urlPatterns = "/logout")
public class ShiroLogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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
