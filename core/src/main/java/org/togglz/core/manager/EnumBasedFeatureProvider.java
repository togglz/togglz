package org.togglz.core.manager;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.metadata.enums.EnumFeatureMetaData;
import org.togglz.core.spi.FeatureProvider;

/**
 * Implementation of {@link FeatureProvider} that uses an Java enum to represent features.
 * 
 * @author Christian Kaltepoth
 */
public class EnumBasedFeatureProvider implements FeatureProvider {

    private final Set<Feature> features = new LinkedHashSet<Feature>();

    public EnumBasedFeatureProvider() {
        // nothing to do
    }

    public EnumBasedFeatureProvider(Class<? extends Feature> featureEnum) {
        addFeatureEnum(featureEnum);
    }

    public EnumBasedFeatureProvider addFeatureEnum(Class<? extends Feature> featureEnum) {
        if (featureEnum == null || !featureEnum.isEnum()) {
            throw new IllegalArgumentException("The featureEnum argument must be an enum");
        }
        features.addAll(Arrays.asList(featureEnum.getEnumConstants()));
        return this;
    }

    @Override
    public Set<Feature> getFeatures() {
        return Collections.unmodifiableSet(features);
    }

    @Override
    public FeatureMetaData getMetaData(Feature feature) {
        // get the _real_ enum feature because the input may be a NamedFeature
        return new EnumFeatureMetaData(getFeatureByName(feature.name()));
    }

    private Feature getFeatureByName(String name) {
        for (Feature f : getFeatures()) {
            if (f.name().equals(name)) {
                return f;
            }
        }
        throw new IllegalArgumentException("Unknown feature: " + name);
    }

}
