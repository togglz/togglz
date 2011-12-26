package de.chkal.togglz.core.spi;

import java.util.Collection;

public interface BeanFinder {

    <E> Collection<E> find(Class<E> clazz, Object context);

}
