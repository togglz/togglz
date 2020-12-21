package org.togglz.spring.web.spi;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.togglz.servlet.util.HttpServletRequestHolder;
import org.togglz.spring.spi.AbstractSpringBeanFinder;
import org.togglz.spring.util.ContextClassLoaderApplicationContextHolder;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class SpringWebBeanFinder extends AbstractSpringBeanFinder {

    @Override
    protected ApplicationContext getApplicationContext(Object context) {

        // try to get the ServletContext from different sources
        ServletContext servletContext = null;
        if (context instanceof ServletContext) {
            servletContext = (ServletContext) context;
        }
        if (servletContext == null) {
            HttpServletRequest request = HttpServletRequestHolder.get();
            if (request != null) {
                servletContext = request.getServletContext();
            }
        }

        // use the Spring API to obtain the WebApplicationContext
        ApplicationContext applicationContext = null;
        if (servletContext != null) {
            applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        }
        if (applicationContext == null) {
            applicationContext = ContextLoader.getCurrentWebApplicationContext();
        }
        if (applicationContext == null) {
            applicationContext = ContextClassLoaderApplicationContextHolder.get();
        }

        return applicationContext;
    }

}
