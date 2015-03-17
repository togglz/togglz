package org.togglz.core.manager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
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

    private final Map<String, FeatureMetaData> metaDataCache = new HashMap<String, FeatureMetaData>();
    private final Set<Feature> features = new LinkedHashSet<Feature>();

    public EnumBasedFeatureProvider() {
        // nothing to do
    }

    public EnumBasedFeatureProvider(Class<? extends Feature>... featureEnums) {
        if (featureEnums == null) {
            throw new IllegalArgumentException("The featureEnums argument must not be null");
        }
        for (Class<? extends Feature> featureEnum : featureEnums) {
            addFeatureEnum(featureEnum);
        }
    }

    public EnumBasedFeatureProvider addFeatureEnum(Class<? extends Feature> featureEnum) {
        if (featureEnum == null || !featureEnum.isEnum()) {
            throw new IllegalArgumentException("The featureEnum argument must be an enum");
        }
        addFeatures(Arrays.asList(featureEnum.getEnumConstants()));
        return this;
    }

    private void addFeatures(Collection<? extends Feature> newFeatures) {
        for (Feature newFeature : newFeatures) {
            if (metaDataCache.put(newFeature.name(), new EnumFeatureMetaData(newFeature)) != null) {
                throw new IllegalStateException("The feature " + newFeature + " has already been added");
            };
            features.add(newFeature);
        }
    }

    @Override
    public Set<Feature> getFeatures() {
        return Collections.unmodifiableSet(features);
    }

    @Override
    public FeatureMetaData getMetaData(Feature feature) {
        return metaDataCache.get(feature.name());
    }
}
