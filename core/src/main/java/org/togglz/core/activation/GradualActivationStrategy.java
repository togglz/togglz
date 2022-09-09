package org.togglz.core.activation;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
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

    private final Logger log = LoggerFactory.getLogger(GradualActivationStrategy.class);

    public static final String ID = "gradual";
    public static final String PARAM_PERCENTAGE = "percentage";

    @Override
    public String getId() {
        return ID;
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

                int percentage = Integer.parseInt(percentageAsString);

                if (percentage > 0) {
                    int hashCode = Math.abs(calculateHashCode(user, state.getFeature()));
                    return (hashCode % 100) < percentage;
                }

            } catch (NumberFormatException e) {
                log.error("Invalid gradual rollout percentage for feature " + state.getFeature().name() + ": "
                    + percentageAsString);
            }

        }

        return false;

    }

    /**
     * @deprecated Use {@link #calculateHashCode(FeatureUser, Feature)} instead
     */
    @Deprecated
    protected int calculateHashCode(FeatureUser user) {
        return calculateHashCode(user, null);
    }

    protected int calculateHashCode(FeatureUser user, Feature feature) {
        Validate.notNull(user, "user is required");

        return (user.getName().toLowerCase(Locale.ENGLISH).trim() + ":" + (feature != null ? feature.name() : "")).hashCode();
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
                ParameterBuilder
                    .create(PARAM_PERCENTAGE)
                    .label("Percentage")
                    .matching("\\d{1,3}")
                    .description(
                        "Percentage of users for which the feature should be active (i.e. '25' for every fourth user).")
        };
    }

}
