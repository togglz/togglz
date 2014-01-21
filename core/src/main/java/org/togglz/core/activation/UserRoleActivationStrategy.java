package org.togglz.core.activation;

import java.util.Collection;
import java.util.List;

import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;

/**
 * ActivationStrategy implementation based on roles of the current user. As far as user has at least one of configured roles
 * then feature will be active.
 * <p/>
 * Please note this activation strategy is not coupled to any particular security framework and will work with any framework as
 * far as current user has "roles" attribute populated with a set of granted authorities. This is usually a responsibility of an
 * UserProvider.
 * 
 * @author Vasily Ivanov
 */
public class UserRoleActivationStrategy implements ActivationStrategy {

    public static final String ID = "user-role";
    public static final String NAME = "Users by role";

    public static final String PARAM_ROLES_NAME = "roles";
    public static final String PARAM_ROLES_LABEL = "Roles";
    public static final String PARAM_ROLES_DESC = "A list of user roles for which the feature is active.";

    public static final String USER_ATTRIBUTE_ROLES = "roles";

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

            Collection<String> userRoles = 
                (Collection<String>) user.getAttribute(USER_ATTRIBUTE_ROLES);

            if (userRoles != null) {

                String rolesAsString = state.getParameter(PARAM_ROLES_NAME);

                if (Strings.isNotBlank(rolesAsString)) {

                    List<String> roles = Strings.splitAndTrim(rolesAsString, ",");

                    for (String authority : roles) {
                        if (userRoles.contains(authority)) {
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
        return new Parameter[] {
                ParameterBuilder.create(PARAM_ROLES_NAME)
                    .label(PARAM_ROLES_LABEL)
                    .description(PARAM_ROLES_DESC)
                    .largeText()
        };
    }
}
