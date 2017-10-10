package org.togglz.microprofile.activation;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.togglz.core.Feature;
import org.togglz.core.activation.AbstractPropertyDrivenActivationStrategy;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;

/**
 * <p>
 * An activation strategy based on the values of properties accessible within the MicroProfile Config.
 * </p>
 * <p>
 * It can either be based on a given property name, passed via the "{@value #PARAM_NAME}" parameter, or a property name
 * derived from the {@link Feature} itself (e.g. "{@value #DEFAULT_PROPERTY_PREFIX}FEATURE_NAME").
 * </p>
 *
 * @author John D. Ament, based on work in DeltaSpikePropertyActivationStrategy from Alasdair Mercer
 * @see AbstractPropertyDrivenActivationStrategy
 */
public class MicroProfileConfigActivationStrategy extends AbstractPropertyDrivenActivationStrategy {

    public static final String ID = "microprofile-config-property";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "MicroProfile Config Property";
    }

    @Override
    protected String getPropertyValue(FeatureState featureState, FeatureUser user, String name) {
        Config config = ConfigProvider.getConfig();
        return config.getOptionalValue(name, String.class).orElse(null);
    }
}
