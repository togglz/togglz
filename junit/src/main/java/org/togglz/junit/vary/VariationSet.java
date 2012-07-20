package org.togglz.junit.vary;

import java.util.Set;

import org.togglz.core.Feature;

/**
 * This class represents a set of feature state variants.
 * 
 * @author Christian Kaltepoth
 * 
 * @param <F> The feature class
 */
public interface VariationSet<F extends Feature> {

    /**
     * Build the variant set data structure from the current configuration of the class.
     */
    Set<Set<F>> getVariants();

}