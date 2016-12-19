package org.togglz.core.metadata;

import java.io.Serializable;

import org.togglz.core.Feature;

/**
 * 
 * Represents a group of feature flags.
 * 
 * @author Christian Kaltepoth
 * 
 */
public interface FeatureGroup extends Serializable {

    String getLabel();

    boolean contains(Feature feature);

}
