package org.togglz.junit5;

import org.togglz.core.Feature;
import org.togglz.testing.vary.VariationSet;

/**
 * <p>
 *     Provides of {@link VariationSet} to test templates invoked with {@link VaryFeatures}.
 * </p>
 *
 * @see VaryFeatures
 *
 * @author Roland Weisleder
 */
public interface VariationSetProvider {

    VariationSet<? extends Feature> buildVariationSet();

}
