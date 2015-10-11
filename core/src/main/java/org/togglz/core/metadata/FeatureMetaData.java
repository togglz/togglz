package org.togglz.core.metadata;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import java.util.Map;
import java.util.Set;

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
     * Default feature state, if it is not stored in a {@link StateRepository}.
     */
    FeatureState getDefaultFeatureState();

    /**
     * The feature groups to which the feature belongs.
     */
    Set<FeatureGroup> getGroups();

    /**
     * A map of custom feature attributes describing the feature
     */
    Map<String, String> getAttributes();

}