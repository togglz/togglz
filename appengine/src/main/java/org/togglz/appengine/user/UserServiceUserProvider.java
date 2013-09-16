package org.togglz.appengine.user;

import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;

/**
 * UserProvider implementation which leverages AppEngine's UserService.
 * 
 * @author Fábio Franco Uechi
 */
public class UserServiceUserProvider implements UserProvider {

    private UserService userService;

    public UserServiceUserProvider(UserService userService) {
        this.userService = userService;
    }

    @Override
    public FeatureUser getCurrentUser() {
        SimpleFeatureUser featureUser = null;
        User user = userService.getCurrentUser();
        if (user != null) {
            featureUser = new SimpleFeatureUser(user.getUserId(), userService.isUserAdmin());
            featureUser.setAttribute("email", user.getEmail());
            featureUser.setAttribute("nickname", user.getNickname());
        }
        return featureUser;
    }
}
