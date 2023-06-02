package org.togglz.servlet.spi;

import jakarta.servlet.ServletContext;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.spi.BeanFinder;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;

/**
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
 */
public class ServletContextBeanFinder implements BeanFinder {

    @Override
    @SuppressWarnings("unchecked")
    public <E> Collection<E> find(Class<E> clazz, Object context) {
        if (context instanceof ServletContext servletContext) {
            String implClassName = servletContext.getInitParameter(clazz.getName());
            if (implClassName != null && implClassName.trim().length() > 0) {
                return Collections.singletonList(createInstance(implClassName, clazz));
            }
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    static <T> T createInstance(String classname, Class<T> interfaceClazz) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        if (classLoader == null) {
            classLoader = interfaceClazz.getClassLoader();
        }

        try {
            Class<T> clazz = (Class<T>) Class.forName(classname, true, classLoader);
            return clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unknown class: " + classname);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException("Could not create an instance of class: " + classname, e);
        }
    }
}
