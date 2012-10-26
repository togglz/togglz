package org.togglz.core.activation;

import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;

public class UsernameActivationStrategy implements ActivationStrategy {

    @Override
    public boolean isActive(FeatureState state, FeatureUser user) {

        // no user restriction? active!
        if (state.getUsers().isEmpty()) {
            return true;
        }

        // check if user is in user list
        if (user != null && user.getName() != null) {
            for (String username : state.getUsers()) {
                if (username.equals(user.getName())) {
                    return true;
                }
            }
        }
        return false;

    }

    @Override
    public int priority() {
        return 100;
    }

}
