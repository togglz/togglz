package org.togglz.core.activation;

import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;

/**
 * A parameter used to configure an {@link ActivationStrategy}. Users can either use {@link ParameterBuilder} for creating a
 * parameter using a fluent API or implement the interface directly.
 * 
 * @author Christian Kaltepoth
 */
public interface Parameter {

    /**
     * The name of the parameter. This name is used to store and retrieve the parameter from the {@link FeatureState}.
     * 
     * @see FeatureState#getParameter(String)
     * @see FeatureState#setParameter(String, String)
     */
    String getName();

    /**
     * A human readable label that describes the parameter.
     */
    String getLabel();

    /**
     * An optional description of the parameter. Can return <code>null</code>, if no description exists.
     */
    String getDescription();

    /**
     * Returns <code>true</code> if the parameter is optional.
     */
    boolean isOptional();

    /**
     * Returns <code>true</code> if the parameter value is typically large so that it should be displayed with an textarea
     * instead of a simple text input field.
     */
    boolean isLargeText();

    /**
     * This method allows to implement custom validation logic for the parameter.
     */
    boolean isValid(String value);

}
