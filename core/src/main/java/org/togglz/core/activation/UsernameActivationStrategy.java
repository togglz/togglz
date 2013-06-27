package org.togglz.core.activation;

import java.util.List;

import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;

/**
 * Activation strategy that allows to activate features only for certain users.
 * 
 * @author Christian Kaltepoth
 */
public class UsernameActivationStrategy implements ActivationStrategy {

    public static final String ID = "username";

    public static final String PARAM_USERS = "users";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Users by name";
    }

    @Override
    public boolean isActive(FeatureState state, FeatureUser user) {

        String usersAsString = state.getParameter(PARAM_USERS);

        if (Strings.isNotBlank(usersAsString)) {

            List<String> users = Strings.splitAndTrim(usersAsString, ",");

            if (user != null && Strings.isNotBlank(user.getName())) {
                for (String username : users) {
                    if (username.equals(user.getName())) {
                        return true;
                    }
                }
            }

        }
        return false;

    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
                ParameterBuilder.create(PARAM_USERS).label("Users").largeText()
                    .description("A list of users for which the feature is active.")
        };
    }

}
