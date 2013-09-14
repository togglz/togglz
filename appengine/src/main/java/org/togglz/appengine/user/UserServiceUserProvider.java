package org.togglz.appengine.user;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

/**
 *
 */
public class UserServiceUserProvider implements UserProvider {

    private UserService userService = UserServiceFactory.getUserService();

    @Override
    public FeatureUser getCurrentUser() {
        SimpleFeatureUser featureUser = null;
        User user = userService.getCurrentUser();
        if (user != null)  {
            featureUser = new SimpleFeatureUser(user.getUserId(), userService.isUserAdmin());
            featureUser.setAttribute("email", user.getEmail());
            featureUser.setAttribute("nickname", user.getNickname());
        }
        return featureUser;
    }
}