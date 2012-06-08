package org.togglz.servlet.spi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.ServletContext;

import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.spi.BeanFinder;
import org.togglz.core.util.ClassUtils;

/**
 * 
 * This implementation of {@link BeanFinder} allows to register implementations of a given interface by setting a servlet
 * context parameter. See the following configuration for an example how to register the implementation of {@link TogglzConfig}
 * so that this class is able to find it.
 * 
 * <pre>
 *   &lt;context-param&gt;
 *     &lt;param-name&gt;org.togglz.core.config.TogglzConfig&lt;/param-name&gt;
 *     &lt;param-value&gt;com.example.myapp.TogglzConfigImpl&lt;/param-value&gt;
 *   &lt;/context-param&gt;
 * </pre>
 * 
 * @author Christian Kaltepoth
 * 
 */
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
