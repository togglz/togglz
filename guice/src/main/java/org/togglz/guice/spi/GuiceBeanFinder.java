package org.togglz.guice.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;

import org.togglz.core.spi.BeanFinder;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * {@link BeanFinder} implementation for Google Guice. Please note that this implementation only works if you are using
 * GuiceServletContextListener from Guice Servlet for setting up Guice.
 * 
 * @author Fábio Franco Uechi
 * @author Christian Kaltepoth
 */
public class GuiceBeanFinder implements BeanFinder {

    @Override
    public <E> Collection<E> find(Class<E> clazz, Object context) {

        if (context instanceof ServletContext) {
            ServletContext servletContext = (ServletContext) context;

            Injector injector = (Injector) servletContext.getAttribute(Injector.class.getName());
            if (injector != null) {

                List<Binding<E>> bindings = injector.findBindingsByType(TypeLiteral.get(clazz));

                Collection<E> result = new ArrayList<E>(bindings.size());
                for (Binding<E> binding : bindings) {
                    result.add(binding.getProvider().get());
                }
                return result;

            }

        }

        return null;

    }

}