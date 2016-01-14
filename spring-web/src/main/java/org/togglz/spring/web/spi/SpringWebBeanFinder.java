package org.togglz.spring.web.spi;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.togglz.core.spi.BeanFinder;
import org.togglz.servlet.util.HttpServletRequestHolder;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;

public class SpringWebBeanFinder implements BeanFinder {

    @Override
    public <T> Collection<T> find(Class<T> clazz, Object context) {

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
        WebApplicationContext applicationContext = null;
        if (servletContext != null) {
            applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        }
        if (applicationContext == null) {
            applicationContext = ContextLoader.getCurrentWebApplicationContext();
        }

        Collection<T> result = new ArrayList<T>();

        // may be null if Spring hasn't started yet
        if (applicationContext != null) {

            // ask spring about beans of this type
            result.addAll(applicationContext.getBeansOfType(clazz).values());

        }

        return result;

    }

}
