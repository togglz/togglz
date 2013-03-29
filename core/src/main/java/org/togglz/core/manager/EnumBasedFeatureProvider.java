package org.togglz.core.manager;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.metadata.EnumFeatureMetaData;
import org.togglz.core.metadata.FeatureMetaData;
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

    @Override
    public FeatureMetaData getMetaData(Feature feature) {
        // get the _real_ enum feature because the input may be a UntypedFeature
        return new EnumFeatureMetaData(getFeatureByName(feature.name()));
    }

    private Feature getFeatureByName(String name) {
        for (Feature f : featureEnum.getEnumConstants()) {
            if (f.name().equals(name)) {
                return f;
            }
        }
        throw new IllegalArgumentException("Unknown feature: " + name);
    }

}
