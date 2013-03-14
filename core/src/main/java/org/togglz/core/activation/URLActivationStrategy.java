package org.togglz.core.activation;

import org.togglz.core.repository.url.URLStateRepository;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;

import java.util.List;

/**
 * Activation strategy that allows to activate features based on url.
 * this strategy can affect all users or per user.
 *
 * @author Eli Abramovitch
 */
public class URLActivationStrategy implements ActivationStrategy {

    public static final String ID = "url";
    public static final String PARAM_USERS = "users";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "URL";
    }

    /**
     *
     * @param state the state stored in the url state repository
     * @param user the user to check
     * @return true - feature is active for this user, false - feature is inactive for this user
     */
    @Override
    public boolean isActive(FeatureState state, FeatureUser user) {
        URLStateRepository urlStateRepository = URLStateRepository.getInstance();

        String usersAsString = urlStateRepository.getFeatureState(state.getFeature()).getParameter(PARAM_USERS);
        if (Strings.isNotBlank(usersAsString)) {
            List<String> users = Strings.splitAndTrim(usersAsString, ",");
            if (user != null && Strings.isNotBlank(user.getName())) {
                for (String username : users) {
                    if (username.equals(user.getName())) {
                        return true;
                    }
                }
            }
            return false;
        }
        return state.getFeature().isActive();//if users list is empty this feature affects all users
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[]{
                ParameterBuilder.create(PARAM_USERS).label("Usernames").largeText().description("A list of users that will see the feature after it's been activated through the url.")
        };
    }

}