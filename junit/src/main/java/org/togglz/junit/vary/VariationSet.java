package org.togglz.junit.vary;

import org.togglz.core.Feature;

/**
 * @see org.togglz.testing.vary.VariationSet
 * @param <F> The feature class
 *
 * @deprecated use {@link org.togglz.testing.vary.VariationSet} instead
 */
@Deprecated
public interface VariationSet<F extends Feature> extends org.togglz.testing.vary.VariationSet<F> {
}
