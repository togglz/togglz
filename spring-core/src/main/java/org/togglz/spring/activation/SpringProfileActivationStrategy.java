package org.togglz.spring.activation;

import java.util.ArrayList;
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
 * <p>
 * One or more profiles can be specified in a comma-separated value via the "{@value #PARAM_PROFILES}" parameter. This strategy
 * works by only activating the feature if at least one of the profiles are currently active. Profile names are not case
 * sensitive.
 * </p>
 * <p>
 * If a given profile is prefixed with the NOT operator ({@code !}), the feature will only be active if the profile is
 * <b>not</b> active. If the value of the "{@value #PARAM_PROFILES}" parameter was {@code "p1,!p2"}, the feature would only be
 * active if "p1" is active or if "p2" is not active.
 * </p>
 *
 * @author Alasdair Mercer
 */
public class SpringProfileActivationStrategy implements ActivationStrategy {

    public static final String ID = "spring-profile";
    public static final String PARAM_PROFILES = "profiles";

    private static List<String> getActiveProfileNames(ApplicationContext applicationContext) {
        String[] names = applicationContext.getEnvironment().getActiveProfiles();
        List<String> result = new ArrayList<>(names.length);
        for (String name : names) {
            result.add(name.toLowerCase());
        }

        return result;
    }

    private static List<Profile> parseProfiles(FeatureState featureState) {
        List<String> names = Strings.splitAndTrim(featureState.getParameter(PARAM_PROFILES), "[\\s,]+");
        List<Profile> result = new ArrayList<>(names.size());
        for (String name : names) {
            name = name.toLowerCase();
            boolean negated = name.startsWith("!");
            if (negated) {
                name = name.substring(1);
            }

            result.add(new Profile(name, negated));
        }

        return result;
    }

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

        List<String> activeProfileNames = getActiveProfileNames(applicationContext);
        List<Profile> profiles = parseProfiles(featureState);

        for (Profile profile : profiles) {
            if (activeProfileNames.contains(profile.name) != profile.negated) {
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
                .description("A comma-separated list of profile names for which the feature should be active. A profile can be "
                    + "negated by prefixing the name with the NOT operator (!).")
        };
    }

    private static class Profile {

        private final boolean negated;
        private final String name;

        Profile(String name, boolean negated) {
            this.name = name;
            this.negated = negated;
        }
    }
}
