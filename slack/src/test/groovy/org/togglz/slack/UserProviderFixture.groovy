package org.togglz.slack

import org.togglz.core.user.FeatureUser
import org.togglz.core.user.SimpleFeatureUser
import org.togglz.core.user.UserProvider

class UserProviderFixture {

    static UserProvider withUser(String user) {
        return new UserProvider() {
            @Override
            FeatureUser getCurrentUser() {
                return new SimpleFeatureUser(user)
            }
        }
    }
}
