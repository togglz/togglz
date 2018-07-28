package org.togglz.core.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.spi.FeatureProvider;

/**
 * Abstract {@link FeatureProvider} that caches features and metadata for enum objects.
 *
 * @param <T> enum class
 *
 * @author rui.figueira
 */
public abstract class AbstractEnumBasedFeatureProvider<T> implements FeatureProvider {

    private final Map<String, FeatureMetaData> metaDataCache = new HashMap<>();
    private final Map<T, Feature> features = new LinkedHashMap<>();

    @Override
    public Set<Feature> getFeatures() {
        return new LinkedHashSet<>(features.values());
    }

    @Override
    public FeatureMetaData getMetaData(Feature feature) {
        return metaDataCache.get(feature.name());
    }

    public FeatureMetaData getMetaData(Enum<?> enumValue) {
        return getMetaData(features.get(enumValue));
    }

    protected void addFeatures(Collection<? extends T> newEnumValues) {
        for (T newEnumValue : newEnumValues) {
            Feature newFeature = createFeatureFor(newEnumValue);
            if (metaDataCache.put(newFeature.name(), featureMetaDataFor(newEnumValue, newFeature)) != null) {
                throw new IllegalStateException("The feature " + newFeature + " has already been added");
            };
            features.put(newEnumValue, newFeature);
        }
    }

    @Override
    public Feature featureFor(Enum<?> enumValue) {
        return features.get(enumValue);
    }

    protected abstract Feature createFeatureFor(T enumValue);
    protected abstract FeatureMetaData featureMetaDataFor(T enumValue, Feature feature);
}
