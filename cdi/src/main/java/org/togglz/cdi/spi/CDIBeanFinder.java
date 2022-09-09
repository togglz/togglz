package org.togglz.cdi.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import org.togglz.core.spi.BeanFinder;

public class CDIBeanFinder implements BeanFinder {

    public final static String BEAN_MANAGER_JNDI = "java:comp/BeanManager";

    public final static String BEAN_MANAGER_JNDI_TOMCAT = "java:comp/env/BeanManager";

    public final static String SERVLET_CONTEXT_ATTR_WELD_1_1 = "org.jboss.weld.environment.servlet.javax.enterprise.inject.spi.BeanManager";

    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> find(Class<T> clazz, Object context) {

        List<T> result = new ArrayList<T>();

        ServletContext servletContext = null;
        if (context instanceof ServletContext) {
            servletContext = (ServletContext) context;
        }

        BeanManager manager = getBeanManager(servletContext);

        if (manager != null) {

            Set<Bean<?>> beans = manager.getBeans(clazz);

            for (Bean<?> bean : beans) {

                CreationalContext<?> cc = manager.createCreationalContext(bean);

                Object reference = manager.getReference(bean, clazz, cc);
                result.add((T) reference);

            }
        }

        return result;

    }

    private BeanManager getBeanManager(ServletContext servletContext) {

        BeanManager beanManager = null;

        // try to find in ServletContext first
        if (servletContext != null) {
            beanManager = (BeanManager) servletContext.getAttribute(BeanManager.class.getName());
        }

        // try Weld 1.1.x servlet context attribute
        if (beanManager == null && servletContext != null) {
            beanManager = (BeanManager) servletContext.getAttribute(SERVLET_CONTEXT_ATTR_WELD_1_1);
        }

        // try standard JNDI name
        if (beanManager == null) {
            beanManager = getBeanManagerFromJNDI(BEAN_MANAGER_JNDI);
        }

        // try special Tomcat JNDI name
        if (beanManager == null) {
            beanManager = getBeanManagerFromJNDI(BEAN_MANAGER_JNDI_TOMCAT);
        }

        return beanManager;

    }

    private BeanManager getBeanManagerFromJNDI(String jndiName) {

        try {

            InitialContext initialContext = new InitialContext();
            return (BeanManager) initialContext.lookup(jndiName);

        } catch (NamingException e) {
            return null;
        }
    }

}
