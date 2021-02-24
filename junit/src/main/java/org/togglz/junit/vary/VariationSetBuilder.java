package org.togglz.junit.vary;

import org.togglz.core.Feature;

/**
 * @see org.togglz.testing.vary.VariationSetBuilder
 * @param <F> The feature class
 *
 * @deprecated use {@link org.togglz.testing.vary.VariationSetBuilder} instead
 */
@Deprecated
public class VariationSetBuilder<F extends Feature> extends org.togglz.testing.vary.VariationSetBuilder<F>
    implements VariationSet<F> {

    /**
     * @deprecated use {@link org.togglz.testing.vary.VariationSetBuilder#create(Class)} instead
     */
    public static <F extends Feature> VariationSetBuilder<F> create(Class<F> featureClass) {
        return new VariationSetBuilder<F>(featureClass);
    }

    private VariationSetBuilder(Class<F> featureEnum) {
        super(featureEnum);
    }

    @Override
    public VariationSetBuilder<F> vary(F f) {
        super.vary(f);
        return this;
    }

    @Override
    public VariationSetBuilder<F> enable(F f) {
        super.enable(f);
        return this;
    }

    @Override
    public VariationSetBuilder<F> disable(F f) {
        super.disable(f);
        return this;
    }

    @Override
    public VariationSetBuilder<F> enableAll() {
        super.enableAll();
        return this;
    }

    @Override
    public VariationSetBuilder<F> disableAll() {
        super.disableAll();
        return this;
    }
}
