package org.togglz.core.spi;

import java.util.Collection;

import org.togglz.core.manager.TogglzConfig;


/**
 * 
 * This SPI is used by Tooglz to lookup beans that are managed by bean containers like CDI or Spring. Currently Togglz uses this
 * feature only for finding the {@link TogglzConfig} implementation.
 * 
 * @author Christian Kaltepoth
 * 
 */
public interface BeanFinder {

    /**
     * Retrieve a list of all beans of the given type.
     * 
     * @param clazz The type to lookup. In most cases this will be an interface.
     * @param context An optional context that may help the implementation to interact with the bean container. In Servlet
     *        environments this context object is the ServletContext.
     * @return A list of beans, never <code>null</code>
     */
    <E> Collection<E> find(Class<E> clazz, Object context);

}
