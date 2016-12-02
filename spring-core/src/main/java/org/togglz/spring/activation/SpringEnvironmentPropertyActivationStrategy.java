package org.togglz.spring.activation;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.togglz.core.Feature;
import org.togglz.core.activation.Parameter;
import org.togglz.core.activation.ParameterBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;
import org.togglz.spring.util.ContextClassLoaderApplicationContextHolder;

/**
 * <p>
 * An activation strategy based on the values of properties accessible within the Spring environment.
 * </p>
 * <p>
 * It can either be based on a given property name, passed as a parameter, or a property name derived from the
 * {@link Feature} itself (e.g. {@code "togglz.FEATURE_NAME"}).
 * </p>
 *
 * @author Alasdair Mercer
 */
public class SpringEnvironmentPropertyActivationStrategy implements ActivationStrategy {

    public static final String ID = "spring-environment-property";
    public static final String PARAM_NAME = "name";

    private static String getPropertyName(FeatureState featureState) {
        String propertyName = featureState.getParameter(PARAM_NAME);
        if (Strings.isNotBlank(propertyName)) {
            return propertyName;
        }

        return "togglz." + featureState.getFeature().name();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Spring Environment Property";
    }

    @Override
    public boolean isActive(FeatureState featureState, FeatureUser user) {
        ApplicationContext applicationContext = ContextClassLoaderApplicationContextHolder.get();
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext could not be found, which can occur if there is no "
                + "bean for TogglzApplicationContextBinderApplicationListener when TogglzAutoConfiguration is not "
                + "being used");
        }

        Environment environment = applicationContext.getEnvironment();
        return environment.getProperty(getPropertyName(featureState), Boolean.TYPE, false);
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
            ParameterBuilder.create(PARAM_NAME)
                .optional()
                .label("Property Name")
                .description("The name of the environment property to be used to determine whether the feature is "
                    + "enabled.")
        };
    }
}
