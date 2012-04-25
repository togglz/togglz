package org.togglz.shiro.test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.togglz.core.util.Strings;

@WebServlet(urlPatterns = "/login")
public class ShiroLoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String user = request.getParameter("user");
        if (Strings.isBlank(user)) {
            throw new IllegalArgumentException("Missing 'user' parameter!");
        }

        Subject subject = SecurityUtils.getSubject();
        subject.login(new UsernamePasswordToken(user, "secret"));

        if (subject.isAuthenticated()) {
            response.getOutputStream().print("SUCCESS");
        }
        else {
            response.getOutputStream().print("FAILED");
        }

        response.getOutputStream().flush();

    }
}
