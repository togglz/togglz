package org.togglz.core;

/**
 * 
 * This interface has to be implemented by feature enums
 * 
 * @author Christian Kaltepoth
 * 
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
     * Checks whether the feature is active for the current user. As Java enumerations don't support inheritance users have do
     * manually implement this method using the following code:
     * 
     * <pre>
     * &#064;Override
     * public boolean isActive() {
     *     return FeatureContext.getFeatureManager().isActive(this);
     * }
     * </pre>
     * 
     * @return <code>true</code> if the feature is active, <code>false</code> otherwise
     */
    boolean isActive();

}
