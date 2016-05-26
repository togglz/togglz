package org.togglz.core.activation;

import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;

/**
 * Created by ddcchrisk on 5/26/16.
 */
public class PropertyActivationStrategy implements ActivationStrategy{

    public static final String ID = "property";
    public static final String PARAM_PROPERTY = "property";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return PARAM_PROPERTY;
    }

    @Override
    public boolean isActive(FeatureState featureState, FeatureUser user) {
        boolean active = false;
        String parameter = featureState.getParameter(PARAM_PROPERTY);
        String sysprop = System.getProperty(parameter);
        String envProp = System.getenv(parameter);
        return validate(sysprop) || validate(envProp);

    }

    private boolean validate(String prop) {
        return (Strings.isNotBlank(prop) && Boolean.valueOf(prop));
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[]
                {
                        ParameterBuilder.create(PARAM_PROPERTY)
                                .label("Property Based")
                                .description("A system based property for which a feature can be enabled")
                                .largeText()
                };
    }
}
