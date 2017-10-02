package org.togglz.spring.spi;

import org.springframework.context.ApplicationContext;
import org.togglz.core.spi.BeanFinder;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractSpringBeanFinder implements BeanFinder {

    protected abstract ApplicationContext getApplicationContext(Object context);

    @Override
    public <T> Collection<T> find(Class<T> clazz, Object context) {

        Collection<T> result = new ArrayList<T>();

        ApplicationContext applicationContext = getApplicationContext(context);

        while (applicationContext != null) {
            result.addAll(applicationContext.getBeansOfType(clazz).values());
            applicationContext = applicationContext.getParent();
        }

        return result;

    }

}
