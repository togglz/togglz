package org.togglz.core.metadata;

import java.util.Collections;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.group.FeatureGroup;

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
    public boolean isEnabledByDefault() {
        return false;
    }

    @Override
    public Set<FeatureGroup> getGroups() {
        return Collections.emptySet();
    }

}
