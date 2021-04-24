package org.togglz.core.manager;

import org.togglz.core.Feature;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.metadata.enums.EnumFeatureMetaData;
import org.togglz.core.spi.FeatureProvider;

import java.util.*;

/**
 * Implementation of {@link FeatureProvider} that uses an Java enum to represent features.
 *
 * @author Christian Kaltepoth
 */
public class EnumBasedFeatureProvider implements FeatureProvider {

    private Map<String, FeatureMetaData> metaDataCache = null;
    private Set<Feature> features = null;

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
        if (metaDataCache == null) {
            metaDataCache = new HashMap<>();
        }
        for (Feature newFeature : newFeatures) {
            if (metaDataCache.put(newFeature.name(), new EnumFeatureMetaData(newFeature)) != null) {
                throw new IllegalStateException("The feature " + newFeature + " has already been added");
            }
            if (features == null){
                features = new LinkedHashSet<>();
            }
            features.add(newFeature);
        }
    }

    @Override
    public Set<Feature> getFeatures() {
        if (this.features == null){
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(features);
    }

    @Override
    public FeatureMetaData getMetaData(Feature feature) {
        if (metaDataCache == null) {
            throw new IllegalStateException("There are no features added in this provider instance.");
        }
        return metaDataCache.get(feature.name());
    }
}
