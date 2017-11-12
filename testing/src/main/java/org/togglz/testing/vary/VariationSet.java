package org.togglz.testing.vary;

import java.util.Set;

import org.togglz.core.Feature;

/**
 * This class represents a set of feature state variants. It is used to configure unit test that are executed with
 * FeatureVariations (JUnit 4 integration) or VaryFeatures (JUnit 5 integration). The common implementation of this interface
 * is {@link VariationSetBuilder} which allows to build sets dynamically.
 * 
 * @author Christian Kaltepoth
 * 
 * @see VariationSetBuilder
 * @param <F> The feature class
 */
public interface VariationSet<F extends Feature> {

    Class<F> getFeatureClass();

    /**
     * Build the variant set data structure from the current configuration of the class.
     */
    Set<Set<F>> getVariants();
}