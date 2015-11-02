package org.togglz.core.metadata;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * {@link FeatureMetaData} implementation that doesn't provide any information.
 * 
 * @author Christian Kaltepoth
 */
public class EmptyFeatureMetaData implements FeatureMetaData {

    private final Feature feature;

    public EmptyFeatureMetaData(Feature feature) {
        this.feature = feature;
    }

    @Override
    public String getLabel() {
        return feature.name();
    }

    @Override
    public FeatureState getDefaultFeatureState() {
        return new FeatureState(feature, false);
    }

    @Override
    public Set<FeatureGroup> getGroups() {
        return Collections.emptySet();
    }

    @Override
    public Map<String, String> getAttributes() {
        return Collections.emptyMap();
    }

}
