package org.togglz.guice.spi;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import org.togglz.core.spi.BeanFinder;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Fábio Franco Uechi
 */
public class GuiceBeanFinder implements BeanFinder {

    @Inject
    Injector injector;


    @Override
    public <E> Collection<E> find(Class<E> clazz, Object context) {
        List<Binding<E>> bindings = injector.findBindingsByType(TypeLiteral.get(clazz));
        Collection<E> result = new ArrayList<E>(bindings.size());
        for(int i = 0; i < bindings.size(); i++ ) {
            result.add(bindings.get(i).getProvider().get());
        }
        return result;
    }

}