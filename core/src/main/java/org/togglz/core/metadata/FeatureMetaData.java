package org.togglz.core.metadata;

import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.group.FeatureGroup;

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
     * Returns a human readable owner of this feature.
     */
    String getOwner();

    /**
     * Returns a link for information on this feature.
     */
    String getInfoLink();

    /**
     * If the feature should be enabled by default.
     */
    boolean isEnabledByDefault();

    /**
     * The feature groups to which the feature belongs.
     */
    Set<FeatureGroup> getGroups();

}