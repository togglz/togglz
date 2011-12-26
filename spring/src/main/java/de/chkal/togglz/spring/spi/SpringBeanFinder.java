package de.chkal.togglz.spring.spi;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.ServletContext;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import de.chkal.togglz.core.spi.BeanFinder;

public class SpringBeanFinder implements BeanFinder {

    @Override
    public <T> Collection<T> find(Class<T> clazz, Object context) {

        Collection<T> result = new ArrayList<T>();

        // use the Spring API to obtain the WebApplicationContext
        WebApplicationContext applicationContext = null;
        if (context instanceof ServletContext) {
            applicationContext = WebApplicationContextUtils.getWebApplicationContext((ServletContext) context);
        }
        if (applicationContext == null) {
            applicationContext = ContextLoader.getCurrentWebApplicationContext();
        }

        // may be null if Spring hasn't started yet
        if (applicationContext != null) {

            // ask spring about beans of this type
            result.addAll(applicationContext.getBeansOfType(clazz).values());

        }

        return result;

    }

}
