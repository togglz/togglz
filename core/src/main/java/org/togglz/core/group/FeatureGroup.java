package org.togglz.core.group;

import org.togglz.core.Feature;

/**
 * 
 * Represents a group of feature flags.
 * 
 * @author Christian Kaltepoth
 * 
 */
public interface FeatureGroup {

    String getLabel();

    boolean contains(Feature feature);

}
