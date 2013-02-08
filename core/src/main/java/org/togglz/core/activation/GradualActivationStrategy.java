package org.togglz.core.activation;

import java.util.Locale;

import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;
import org.togglz.core.util.Validate;

/**
 * Activation strategy that enables features for a given percentage of users. This strategy is typically used to implement
 * gradual rollouts. The implementation is based on a hashcode created from the name of the acting user which is calculated by
 * {@link #calculateHashCode(FeatureUser)}.
 * 
 * @author Christian Kaltepoth
 */
public class GradualActivationStrategy implements ActivationStrategy {

    private final Log log = LogFactory.getLog(GradualActivationStrategy.class);
    
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

        if (user != null && Strings.isNotBlank(user.getName())) {

            String percentageAsString = state.getParameter(PARAM_PERCENTAGE);
            try {

                int percentage = Integer.valueOf(percentageAsString);

                if (percentage > 0) {
                    int hashCode = calculateHashCode(user);
                    return (hashCode % 100) < percentage;
                }

            } catch (NumberFormatException e) {
                log.error("Invalid gradual rollout percentage for feature " + state.getFeature().name() + ": "
                    + percentageAsString);
            }

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
