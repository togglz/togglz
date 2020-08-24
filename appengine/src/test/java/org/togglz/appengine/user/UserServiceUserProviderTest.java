package org.togglz.appengine.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.togglz.core.user.FeatureUser;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for {@link UserServiceUserProvider}.
 * 
 * @author Fábio Franco Uechi
 */
public class UserServiceUserProviderTest {

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig());
    private UserServiceUserProvider userProvider;
    private UserService userService = UserServiceFactory.getUserService();

    @BeforeEach
    public void setup() {
        helper.setUp();
        userProvider = new UserServiceUserProvider(userService);
    }

    @AfterEach
    public void tearDown() {
        helper.tearDown();
    }

    @Test
    public void userIsNotLoggedIn() {
        helper.setEnvIsLoggedIn(false);
        FeatureUser user = userProvider.getCurrentUser();
        assertNull(user);
    }

    @Test
    public void userIsLoggedIn() {
        helper.setEnvIsLoggedIn(true)
            .setEnvEmail("user@togglz.org")
            .setEnvAuthDomain("togglz.org");

        UserService userService = UserServiceFactory.getUserService();
        FeatureUser user = userProvider.getCurrentUser();
        assertFalse(user.isFeatureAdmin());
        assertEquals(userService.getCurrentUser().getUserId(), user.getName());
        assertEquals(userService.getCurrentUser().getEmail(), user.getAttribute("email"));
        assertEquals(userService.getCurrentUser().getNickname(), user.getAttribute("nickname"));
    }

    @Test
    public void userIsLoggedInAsAdmin() {
        helper.setEnvIsLoggedIn(true)
            .setEnvIsAdmin(true)
            .setEnvEmail("admin@togglz.org")
            .setEnvAuthDomain("togglz.org");

        UserService userService = UserServiceFactory.getUserService();
        FeatureUser user = userProvider.getCurrentUser();
        assertTrue(user.isFeatureAdmin());
        assertEquals(userService.getCurrentUser().getUserId(), user.getName());
        assertEquals(userService.getCurrentUser().getEmail(), user.getAttribute("email"));
        assertEquals(userService.getCurrentUser().getNickname(), user.getAttribute("nickname"));
    }

}
