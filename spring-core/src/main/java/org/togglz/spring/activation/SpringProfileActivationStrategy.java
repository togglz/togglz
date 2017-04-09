package org.togglz.spring.activation;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.ApplicationContext;
import org.togglz.core.activation.AbstractTokenizedActivationStrategy;
import org.togglz.core.activation.Parameter;
import org.togglz.core.activation.ParameterBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;
import org.togglz.spring.util.ContextClassLoaderApplicationContextHolder;

/**
 * <p>
 * An activation strategy based on the profiles that are active within the Spring environment.
 * </p>
 * <p>
 * One or more profiles can be specified in a comma-separated value via the "{@value #PARAM_PROFILES}" parameter. This
 * strategy works by only activating the feature if at least one of the profiles are currently active. Profile names are
 * not case sensitive.
 * </p>
 * <p>
 * If a given profile is prefixed with the NOT operator ({@code !}), the feature will only be active if the profile is
 * <b>not</b> active. If the value of the "{@value #PARAM_PROFILES}" parameter was {@code "p1,!p2"}, the feature would
 * only be active if "p1" is active or if "p2" is not active.
 * </p>
 *
 * @author Alasdair Mercer
 * @see AbstractTokenizedActivationStrategy
 */
public class SpringProfileActivationStrategy extends AbstractTokenizedActivationStrategy {

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

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Spring Profile";
    }

    @Override
    public boolean isActive(FeatureState featureState, FeatureUser user, List<Token> tokens) {
        ApplicationContext applicationContext = ContextClassLoaderApplicationContextHolder.get();
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext could not be found, which can occur if there is no "
                + "bean for TogglzApplicationContextBinderApplicationListener when TogglzAutoConfiguration is not "
                + "being used");
        }

        List<String> activeProfileNames = getActiveProfileNames(applicationContext);

        for (Token token : tokens) {
            if (activeProfileNames.contains(token.getValue()) != token.isNegated()) {
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
                .description("A comma-separated list of profile names for which the feature should be active. A "
                    + "profile can be negated by prefixing the name with the NOT operator (!).")
        };
    }

    @Override
    public String getTokenParameterName() {
        return PARAM_PROFILES;
    }

    @Override
    public TokenTransformer getTokenParameterTransformer() {
        return new TokenTransformer() {
            @Override
            public String transform(String value) {
                return value.toLowerCase();
            }
        };
    }
}
