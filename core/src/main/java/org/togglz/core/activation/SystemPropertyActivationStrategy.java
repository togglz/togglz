package org.togglz.core.activation;

import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;

/**
 * Created by ddcchrisk on 5/26/16.
 */
public class SystemPropertyActivationStrategy implements ActivationStrategy{

    public static final String ID = "property";
    public static final String PARAM_PROPERTY_NAME = "name";
    public static final String PARAM_PROPERTY_VALUE = "value";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return PARAM_PROPERTY_NAME;
    }

    @Override
    public boolean isActive(FeatureState featureState, FeatureUser user) {
        String stateName = featureState.getParameter(PARAM_PROPERTY_NAME);
        String stateValue = featureState.getParameter(PARAM_PROPERTY_VALUE);
        String propValue = System.getProperty(stateName);
        return validate(propValue, stateValue);

    }

    private boolean validate(String sysValue, String stateValue) {
        return (Strings.isNotBlank(sysValue) && (sysValue.equals(stateValue) || Boolean.valueOf(sysValue)));
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[]
                {
                        ParameterBuilder.create(PARAM_PROPERTY_NAME)
                                .label("System Property")
                                .description("A system property that can be set for which a feature should be active")
                                .largeText()
                };
    }
}
