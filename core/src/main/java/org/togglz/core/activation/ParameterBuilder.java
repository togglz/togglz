package org.togglz.core.activation;

import java.util.regex.Pattern;

import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.Validate;

/**
 * Fluent API for creating {@link Parameter} instances.
 * 
 * @author Christian Kaltepoth
 */
public class ParameterBuilder implements Parameter {

    private final String name;
    private String label;
    private Pattern pattern;
    private boolean optional;
    private String description;
    private boolean largeText;

    /**
     * Creates a new builder for a parameter with the given name. The name is used to store and retrieve the parameter from the
     * {@link FeatureState}.
     * 
     * @see FeatureState#getParameter(String)
     * @see FeatureState#setParameter(String, String)
     */
    public static ParameterBuilder create(String name) {
        return new ParameterBuilder(name);
    }

    private ParameterBuilder(String name) {
        Validate.notNull(name, "id is required");
        this.name = name;
        this.label = name;
    }

    /**
     * A custom human readable label for the parameter. If no custom label is set, the builder will use the name of the
     * parameter as a label.
     */
    public ParameterBuilder label(String name) {
        Validate.notNull(name, "name is required");
        this.label = name;
        return this;
    }

    /**
     * Sets a regular expression that must match for parameter values to be considered as valid.
     */
    public ParameterBuilder matching(String regex) {
        Validate.notNull(regex, "regex is required");
        this.pattern = Pattern.compile(regex);
        return this;
    }

    /**
     * Configures the parameter to be optional.
     */
    public ParameterBuilder optional() {
        this.optional = true;
        return this;
    }

    /**
     * Specifies that the parameter typically has large texts as a value
     */
    public ParameterBuilder largeText() {
        this.largeText = true;
        return this;
    }

    /**
     * Sets an optional description for the parameter displayed in the admin console.
     */
    public ParameterBuilder description(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public boolean isValid(String value) {
        if (pattern != null) {
            return pattern.matcher(value).matches();
        }
        return true;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    @Override
    public boolean isLargeText() {
        return largeText;
    }

    @Override
    public String getDescription() {
        return description;
    }

}
