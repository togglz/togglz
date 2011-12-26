package de.chkal.togglz.servlet.spi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.ServletContext;

import de.chkal.togglz.core.spi.BeanFinder;
import de.chkal.togglz.core.util.ClassUtils;

public class ServletContextBeanFinder implements BeanFinder {

    @Override
    public <E> Collection<E> find(Class<E> clazz, Object context) {

        if (context instanceof ServletContext) {

            ServletContext servletContext = (ServletContext) context;

            String implClassName = servletContext.getInitParameter(clazz.getName());

            if (implClassName != null && implClassName.trim().length() > 0) {

                return Arrays.asList(ClassUtils.createInstance(implClassName, clazz));

            }

        }
        
        return Collections.emptyList();

    }

}
