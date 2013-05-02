package org.togglz.core.user;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class SingleUserProviderTest {

    @Test
    public void canProvideNamedUser() {
        String username = "named-user";
        boolean featureAdmin = true;
        UserProvider userProvider = new SingleUserProvider(username, featureAdmin);
        FeatureUser user = userProvider.getCurrentUser();
        assertThat(user.getName(), equalTo(username));
        assertThat(user.isFeatureAdmin(), equalTo(featureAdmin));
    }

}
