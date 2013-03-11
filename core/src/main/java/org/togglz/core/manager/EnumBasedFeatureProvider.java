package org.togglz.core.manager;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.spi.FeatureProvider;

/**
 * Implementation of {@link FeatureProvider} that uses an Java enum to represent features.
 * 
 * @author Christian Kaltepoth
 */
public class EnumBasedFeatureProvider implements FeatureProvider {

    private final Class<? extends Feature> featureEnum;

    public EnumBasedFeatureProvider(Class<? extends Feature> featureEnum) {
        if (featureEnum == null || !featureEnum.isEnum()) {
            throw new IllegalArgumentException("The featureEnum argument must be an enum");
        }
        this.featureEnum = featureEnum;
    }

    @Override
    public Set<Feature> getFeatures() {
        return new LinkedHashSet<Feature>(Arrays.asList(featureEnum.getEnumConstants()));
    }

}
