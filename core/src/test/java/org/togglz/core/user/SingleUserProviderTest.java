package org.togglz.core.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SingleUserProviderTest {

    @Test
    void canProvideNamedUser() {
        String username = "named-user";
        boolean featureAdmin = true;
        UserProvider userProvider = new SingleUserProvider(username, featureAdmin);
        FeatureUser user = userProvider.getCurrentUser();
        assertEquals(username, user.getName());
        assertEquals(featureAdmin, user.isFeatureAdmin());
    }

}
