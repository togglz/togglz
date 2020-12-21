package org.togglz.testing.vary;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.util.Validate;

/**
 * Default implementation of {@link VariationSet} that allows to build the set dynamically.
 *
 * @param <F> The feature class
 * @author Christian Kaltepoth
 */
public class VariationSetBuilder<F extends Feature> implements VariationSet<F> {

    public static <F extends Feature> VariationSetBuilder<F> create(Class<F> featureClass) {
        return new VariationSetBuilder<>(featureClass);
    }

    private final Class<F> featureClass;

    private final Set<F> featuresToVary = new HashSet<F>();
    private final Set<F> featuresToEnable = new HashSet<F>();
    private final Set<F> featuresToDisable = new HashSet<F>();

    protected VariationSetBuilder(Class<F> featureEnum) {
        Validate.notNull(featureEnum, "The featureEnum argument is required");
        Validate.isTrue(featureEnum.isEnum(), "This class only works with feature enums");
        this.featureClass = featureEnum;
    }

    /**
     * Vary this feature in the variation set.
     */
    public VariationSetBuilder<F> vary(F f) {
        featuresToVary.add(f);
        featuresToEnable.remove(f);
        featuresToDisable.remove(f);
        return this;
    }

    /**
     * Enable this feature in the variation set.
     */
    public VariationSetBuilder<F> enable(F f) {
        featuresToVary.remove(f);
        featuresToEnable.add(f);
        featuresToDisable.remove(f);
        return this;
    }

    /**
     * Disable this feature in the variation set.
     */
    public VariationSetBuilder<F> disable(F f) {
        featuresToVary.remove(f);
        featuresToEnable.remove(f);
        featuresToDisable.add(f);
        return this;
    }

    /**
     * Enable all features in the variation set.
     */
    public VariationSetBuilder<F> enableAll() {
        for (F f : featureClass.getEnumConstants()) {
            enable(f);
        }
        return this;
    }

    /**
     * Disable all features in the variation set.
     */
    public VariationSetBuilder<F> disableAll() {
        for (F f : featureClass.getEnumConstants()) {
            disable(f);
        }
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.togglz.junit.vary.VariationSet#getVariants()
     */
    @Override
    public Set<Set<F>> getVariants() {

        // start with a single variant with all feature disabled
        Set<Set<F>> variantSet = new LinkedHashSet<>();
        variantSet.add(new HashSet<F>());

        for (F feature : featureClass.getEnumConstants()) {

            // enable the feature in all variants
            if (featuresToEnable.contains(feature)) {
                for (Set<F> variant : variantSet) {
                    variant.add(feature);
                }
            }

            // disable the feature in all variants
            else if (featuresToDisable.contains(feature)) {
                for (Set<F> variant : variantSet) {
                    variant.remove(feature);
                }
            }

            // copy the existing variants, enable the feature in the copy, and merge them
            else if (featuresToVary.contains(feature)) {
                Set<Set<F>> copy = deepCopy(variantSet);
                for (Set<F> variant : copy) {
                    variant.add(feature);
                }
                variantSet.addAll(copy);
            }

        }
        return variantSet;
    }

    private Set<Set<F>> deepCopy(Set<Set<F>> src) {
        Set<Set<F>> copy = new LinkedHashSet<Set<F>>();
        for (Set<F> variant : src) {
            copy.add(new HashSet<F>(variant));
        }
        return copy;
    }

    @Override
    public Class<F> getFeatureClass() {
        return featureClass;
    }

}
