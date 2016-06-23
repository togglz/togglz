package org.togglz.spring.spi;

import org.springframework.context.ApplicationContext;
import org.togglz.spring.util.ContextClassLoaderApplicationContextHolder;

/**
 * Simple implementation of {@link org.togglz.core.spi.BeanFinder} which uses
 * {@link ContextClassLoaderApplicationContextHolder}.
 */
public class SpringCoreBeanFinder extends AbstractSpringBeanFinder {

    @Override
    protected ApplicationContext getApplicationContext(Object context) {
        return ContextClassLoaderApplicationContextHolder.get();
    }

}
