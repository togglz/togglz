package org.togglz.core.activation;

import java.util.Locale;

import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;
import org.togglz.core.util.Validate;

public class GradualActivationStrategy implements ActivationStrategy {

    public static final String PARAM_PERCENTAGE = "percentage";

    @Override
    public String getId() {
        return "gradual";
    }

    @Override
    public String getName() {
        return "Gradual rollout";
    }

    @Override
    public boolean isActive(FeatureState state, FeatureUser user) {

        Integer percentage = Integer.valueOf(state.getParameter(PARAM_PERCENTAGE));

        if (user != null && Strings.isNotBlank(user.getName())) {
            int hashCode = calculateHashCode(user);
            return (hashCode % 100) <= percentage;
        }

        return false;

    }

    protected int calculateHashCode(FeatureUser user) {
        Validate.notNull(user, "user is required");
        return user.getName().toLowerCase(Locale.ENGLISH).trim().hashCode();
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
                ParameterBuilder.create(PARAM_PERCENTAGE).label("Percentage").matching("\\d{1,3}")
                    .description("Percentage of users for which the feature should be active (i.e. '25' for every fourth user).")
        };
    }

}
