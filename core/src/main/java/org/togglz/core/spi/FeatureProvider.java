package org.togglz.core.spi;

import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.metadata.enums.EnumFeatureMetaData;

/**
 * Implementations of this interface are responsible for providing feature base data. The default implementation
 * {@link EnumFeatureMetaData} for example is used if features a defined using a feature enum. Users can provide custom
 * implementations to support something like a dynamic list of features.
 * 
 * @author Christian Kaltepoth
 */
public interface FeatureProvider {

    /**
     * Returns a list of all valid features, never <code>null</code>.
     */
    Set<Feature> getFeatures();

    /**
     * Returns the {@link FeatureMetaData} for the given feature. May return null if no metadata is available.
     */
    FeatureMetaData getMetaData(Feature feature);

}
