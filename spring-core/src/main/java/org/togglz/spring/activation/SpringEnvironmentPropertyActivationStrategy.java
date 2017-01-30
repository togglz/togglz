package org.togglz.spring.activation;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.togglz.core.Feature;
import org.togglz.core.activation.AbstractPropertyDrivenActivationStrategy;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;
import org.togglz.spring.util.ContextClassLoaderApplicationContextHolder;

/**
 * <p>
 * An activation strategy based on the values of properties accessible within the Spring environment.
 * </p>
 * <p>
 * It can either be based on a given property name, passed via the "{@value #PARAM_NAME}" parameter, or a property name
 * derived from the {@link Feature} itself (e.g. "{@value #DEFAULT_PROPERTY_PREFIX}FEATURE_NAME").
 * </p>
 *
 * @author Alasdair Mercer
 * @see AbstractPropertyDrivenActivationStrategy
 */
public class SpringEnvironmentPropertyActivationStrategy extends AbstractPropertyDrivenActivationStrategy {

    public static final String ID = "spring-environment-property";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Spring Environment Property";
    }

    @Override
    protected String getPropertyValue(FeatureState featureState, FeatureUser user, String name) {
        ApplicationContext applicationContext = ContextClassLoaderApplicationContextHolder.get();
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext could not be found, which can occur if there is no "
                + "bean for TogglzApplicationContextBinderApplicationListener when TogglzAutoConfiguration is not "
                + "being used");
        }

        Environment environment = applicationContext.getEnvironment();
        return environment.getProperty(name);
    }
}
