package org.togglz.core;

/**
 *
 * <p>
 * This interface represents a feature and is typically implemented by the feature enum.
 * </p>
 *
 * <p>
 * Usually it makes sense to implement the following method which allows a very easy way to check the status of a feature.
 * </p>
 *
 * <pre>
 * public boolean isActive() {
 *     return FeatureContext.getFeatureManager().isActive(this);
 * }
 * </pre>
 *
 * <p>
 * Please note that in Togglz 2.0 the <code>isActive()</code> method isn't defined in the interface any more. So when updating
 * to Togglz 2.0, you will have to remove the {@link Override} annotation from your implementation of the method.
 * </p>
 *
 * <p>
 * Please note that in Togglz 2.6.0.Kotlin the <code>name()</code> method isn't defined in the interface anymore.
 * It has been renamed to <code>id()</code>. So when updating to Togglz 2.6.0.Kotlin, you will have to implement the method.
 * </p>
 *
 * @author Christian Kaltepoth
 *
 */
public interface Feature {

    /**
     * Returns a textual representation of the feature.
     *
     * @return Id of the feature
     */
    String id();

}
