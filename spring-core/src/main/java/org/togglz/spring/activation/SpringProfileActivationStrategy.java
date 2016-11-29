package org.togglz.spring.activation;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.ApplicationContext;
import org.togglz.core.activation.Parameter;
import org.togglz.core.activation.ParameterBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;
import org.togglz.spring.util.ContextClassLoaderApplicationContextHolder;

/**
 * <p>
 * An activation strategy based on the profiles that are active within the Spring environment.
 * </p>
 *
 * @author Alasdair Mercer
 */
public class SpringProfileActivationStrategy implements ActivationStrategy {

    public static final String ID = "spring-profile";
    public static final String PARAM_PROFILES = "profiles";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Spring Profile";
    }

    @Override
    public boolean isActive(FeatureState featureState, FeatureUser user) {
        ApplicationContext applicationContext = ContextClassLoaderApplicationContextHolder.get();
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext could not be found, which can occur if there is no bean for "
                + "TogglzApplicationContextBinderApplicationListener when TogglzAutoConfiguration is not being used");
        }

        List<String> activeProfiles = Arrays.asList(applicationContext.getEnvironment().getActiveProfiles());
        List<String> profiles = Strings.splitAndTrim(featureState.getParameter(PARAM_PROFILES), "[\\s,]+");

        for (String profile : profiles) {
            if (activeProfiles.contains(profile)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
            ParameterBuilder.create(PARAM_PROFILES)
                .label("Profile Names")
                .description("A comma-separated list of profile names for which the feature should be active.")
        };
    }
}
