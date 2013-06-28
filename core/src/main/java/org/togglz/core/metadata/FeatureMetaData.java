package org.togglz.core.metadata;

import java.util.Map;
import java.util.Set;

import org.togglz.core.Feature;

/**
 * Metadata of a {@link Feature}.
 * 
 * @author Christian Kaltepoth
 */
public interface FeatureMetaData {

    /**
     * Returns a human readable name of the feature.
     */
    String getLabel();

    /**
     * If the feature should be enabled by default.
     */
    boolean isEnabledByDefault();

    /**
     * The feature groups to which the feature belongs.
     */
    Set<FeatureGroup> getGroups();

    /**
     * A map of custom feature attributes describing the feature
     */
    Map<String, String> getAttributes();

}