package org.togglz.deltaspike.activation;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.togglz.core.Feature;
import org.togglz.core.activation.AbstractPropertyDrivenActivationStrategy;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;

/**
 * <p>
 * An activation strategy based on the values of properties accessible within the DeltaSpike configuration.
 * </p>
 * <p>
 * It can either be based on a given property name, passed via the "{@value #PARAM_NAME}" parameter, or a property name
 * derived from the {@link Feature} itself (e.g. "{@value #DEFAULT_PROPERTY_PREFIX}FEATURE_NAME").
 * </p>
 *
 * @author Alasdair Mercer
 * @see AbstractPropertyDrivenActivationStrategy
 */
public class DeltaSpikePropertyActivationStrategy extends AbstractPropertyDrivenActivationStrategy {

    public static final String ID = "deltaspike-property";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "DeltaSpike Property";
    }

    @Override
    protected String getPropertyValue(FeatureState featureState, FeatureUser user, String name) {
        return ConfigResolver.getProjectStageAwarePropertyValue(name);
    }
}
