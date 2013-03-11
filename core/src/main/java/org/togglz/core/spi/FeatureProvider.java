package org.togglz.core.spi;

import java.util.Set;

import org.togglz.core.Feature;

public interface FeatureProvider {
    
    /**
     * Returns a list of all valid features, never <code>null</code>.
     */
    public Set<Feature> getFeatures();

}
