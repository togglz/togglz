package org.togglz.core.activation;


import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;

/**
 * ActivationStrategy based on a key value pair system property. To activate, the value of the property must match
 * the string value, not case sensitive.
 * <p>
 * Created by Chris Kelley on 5/26/16.
 */
public class SystemPropertyActivationStrategy extends AbstractPropertyDrivenActivationStrategy implements ActivationStrategy {

    public static final String ID = "property";
    public static final String PARAM_PROPERTY_NAME = "system-property";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "System Property";
    }

    @Override
    protected String getPropertyValue(FeatureState featureState, FeatureUser user, String name) {
        return System.getProperty(name);
    }

    @Override
    protected String getPropertyNameParam() {
        return PARAM_PROPERTY_NAME;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[]
                {
                        ParameterBuilder.create(PARAM_PROPERTY_NAME)
                                .optional()
                                .label("System Property Name")
                                .description("A system property name that can be set for which a feature should be active"),
                        ParameterBuilder.create(PARAM_PROPERTY_VALUE)
                                .optional()
                                .label("System Property Value")
                                .description("Enable the feature when this value matches the system property value")
                };
    }
}
