package org.togglz.core;

import org.togglz.core.context.FeatureContext;

/**
 * <p>
 * This interface represents a feature and is typically implemented by the feature enum.
 * </p>
 *
 * @author Christian Kaltepoth
 */
public interface Feature {

    /**
     * Returns a textual representation of the feature. This method is implicitly implemented as feature typically are
     * enumerations.
     *
     * @return Name of the feature
     */
    String name();

    /**
     * Checks whether the feature is active for the current user.
     *
     * @return <code>true</code> if the feature is active, <code>false</code> otherwise
     */
    default boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }
}
