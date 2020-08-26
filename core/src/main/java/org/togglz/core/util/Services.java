package org.togglz.core.util;

import org.togglz.core.util.Weighted.WeightedComparator;

import java.util.*;

/**
 * 
 * Helper class to lookup SPI implementations using the {@link ServiceLoader}.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class Services {

    private static final WeightedComparator WEIGHTED_COMPARATOR = new WeightedComparator();

    /**
     * Lookup implementations of the supplied SPI. Please note that the order in which the implementations will occur in the
     * collection is not specified.
     */
    public static <E> Collection<E> get(Class<? extends E> service) {
        Iterator<? extends E> implementations = ServiceLoader.load(service).iterator();

        Collection<E> result = new ArrayList<E>();
        while (implementations.hasNext()) {
            result.add(implementations.next());
        }
        return result;

    }

    /**
     * Lookup implementations of the supplied SPI. This method requires the SPI to extends {@link Weighted} and therefore is
     * able to return a sorted list of implementations.
     */
    public static <E extends Weighted> List<E> getSorted(Class<? extends E> service) {
        List<E> result = new ArrayList<E>(get(service));
        result.sort(WEIGHTED_COMPARATOR);
        return result;
    }

}
