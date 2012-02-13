package org.togglz.servlet.spi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.ServletContext;

import org.togglz.core.spi.BeanFinder;
import org.togglz.core.util.ClassUtils;


public class ServletContextBeanFinder implements BeanFinder {

    @Override
    @SuppressWarnings("unchecked")
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
