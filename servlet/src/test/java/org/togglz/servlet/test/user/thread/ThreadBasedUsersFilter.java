package org.togglz.servlet.test.user.thread;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.thread.ThreadLocalUserProvider;

@WebFilter(urlPatterns = "/*")
public class ThreadBasedUsersFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        String username = request.getParameter("user");
        if (username == null) {
            throw new IllegalArgumentException("Query parameter 'user' must be set!");
        }

        ThreadLocalUserProvider.bind(new SimpleFeatureUser(username, "ck".equals(username)));

        try {
            chain.doFilter(request, response);
        } finally {
            ThreadLocalUserProvider.bind(null);
        }

    }

    @Override
    public void destroy() {
    }

}
