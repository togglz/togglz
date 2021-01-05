package org.togglz.core.spi;

import org.togglz.core.activation.Parameter;
import org.togglz.core.activation.ParameterBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.UserProvider;

/**
 * This interface represents a custom strategy for deciding whether a feature is active or not. Togglz ships with a few default
 * implementations.
 * 
 * @author Christian Kaltepoth
 */
public interface ActivationStrategy {

    /**
     * A unique id for this strategy. This id is used to persist the selected strategy in the {@link StateRepository}.
     */
    String getId();

    /**
     * A human readable name of the strategy. This name is used to in the admin console to represent the strategy.
     */
    String getName();

    /**
     * This method is responsible to decide whether a feature is active or not. The implementation can use the custom
     * configuration parameters of the strategy stored in the feature state and information from the currently acting user to
     * find a decision.
     * 
     * @param featureState The feature state which represents the current configuration of the feature. The implementation of
     *        the method typically uses {@link FeatureState#getParameter(String)} to access custom configuration parameter
     *        values.
     * @param user The user for which to decide whether the feature is active. May be <code>null</code> if the user could not be
     *        identified by the {@link UserProvider}.
     * 
     * @return <code>true</code> if the feature should be active, else <code>false</code>
     */
    boolean isActive(FeatureState featureState, FeatureUser user);

    /**
     * <p>
     * Returns the list of configuration parameter definitions for the strategy. Parameters are typically built using a
     * {@link ParameterBuilder} class but users can also create custom implementations of the {@link Parameter} interface.
     * </p>
     * 
     * <p>
     * Example:
     * </p>
     * 
     * <pre>
     * public Parameter[] getParameters() {
     *     return new Parameter[] {
     *             ParameterBuilder.create(&quot;country&quot;).label(&quot;Country Code&quot;).matching(&quot;[A-Z]+&quot;)
     *     };
     * }
     * </pre>
     * 
     * @see ParameterBuilder
     */
    Parameter[] getParameters();

}
