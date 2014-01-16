package org.togglz.spring.security;

import java.util.List;
import java.util.Set;
import org.togglz.core.activation.Parameter;
import org.togglz.core.activation.ParameterBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;

/**
 * ActivationStrategy implementation based on authorities granted to the current user. As far as user has at least one
 * of configured authorities then feature will be active.
 * <p/>
 * Please note this activation strategy is not coupled to any particular security framework and will work with any
 * framework as far as current user has "authorities" attribute populated with a set of granted authorities. This is
 * usually a responsibility of an UserProvider.
 *
 * @author Vasily Ivanov
 */
public class UserAuthorityActivationStrategy implements ActivationStrategy {

    public static final String ID = "user-authority";
    public static final String NAME = "User Authority";

    public static final String PARAM_AUTHORITIES_NAME = "authorities";
    public static final String PARAM_AUTHORITIES_LABEL = "Authorities";
    public static final String PARAM_AUTHORITIES_DESC = "A list of authorities for which the feature is active.";

    public static final String USER_ATTRIBUTE_AUTHORITIES = "authorities";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isActive(FeatureState state, FeatureUser user) {

        if (user != null) {

            Set<String> userAuthorities = (Set<String>) user.getAttribute(USER_ATTRIBUTE_AUTHORITIES);

            if (userAuthorities != null) {

                String authoritiesAsString = state.getParameter(PARAM_AUTHORITIES_NAME);

                if (Strings.isNotBlank(authoritiesAsString)) {

                    List<String> authorities = Strings.splitAndTrim(authoritiesAsString, ",");

                    for (String authority : authorities) {
                        if (userAuthorities.contains(authority)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[]{
                ParameterBuilder.create(PARAM_AUTHORITIES_NAME).label(PARAM_AUTHORITIES_LABEL).largeText()
                        .description(PARAM_AUTHORITIES_DESC)
        };
    }
}
