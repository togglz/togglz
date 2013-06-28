package org.togglz.core.metadata;

import org.togglz.core.Feature;

/**
 * Simple version of a {@link FeatureGroup} that gets all information in the constructor
 */
public class SimpleFeatureGroup implements FeatureGroup {

    private final String label;

    public SimpleFeatureGroup(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public boolean contains(Feature feature) {
        throw new UnsupportedOperationException();
    }

}