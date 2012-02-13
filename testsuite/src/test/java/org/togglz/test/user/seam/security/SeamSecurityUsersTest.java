package org.togglz.test.user.seam.security;

import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.test.Deployments;
import org.togglz.test.user.UserDependentFeature;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;


@RunWith(Arquillian.class)
public class SeamSecurityUsersTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.getSeamSecurityArchive()

                // Seam Security
                .addClass(SeamSecurityAuthenticator.class)
                .addClass(SeamSecurityAuthorizer.class)
                .addClass(SeamSecurityLoginServlet.class)
                .addClass(SeamSecurityLogoutServlet.class)

                // Togglz
                .addClass(SeamSecurityUsersConfiguration.class)
                .addClass(UserDependentFeature.class);

    }

    @ArquillianResource
    private URL url;

    @Test
    public void testSeamSecurityWithoutLogin() throws Exception {

        WebClient client = new WebClient();
        TextPage page = client.getPage(url + "features");
        assertTrue(page.getContent().contains("DISABLED = false"));
        assertTrue(page.getContent().contains("ENABLED_FOR_ALL = true"));
        assertTrue(page.getContent().contains("ENABLED_FOR_CK = false"));

        TextPage userPage = client.getPage(url + "user");
        assertTrue(userPage.getContent().contains("USER = null"));
        assertTrue(userPage.getContent().contains("ADMIN = null"));

    }

    @Test
    public void testSeamSecurityFeatureAdminFlagAdminUser() throws Exception {

        WebClient client = new WebClient();

        TextPage beforeLogin = client.getPage(url + "user");
        assertTrue(beforeLogin.getContent().contains("USER = null"));
        assertTrue(beforeLogin.getContent().contains("ADMIN = null"));

        TextPage loginPage = client.getPage(url + "login?user=ck");
        assertTrue(loginPage.getContent().contains("SUCCESS"));

        TextPage afterLogin = client.getPage(url + "user");
        assertTrue(afterLogin.getContent().contains("USER = ck"));
        assertTrue(afterLogin.getContent().contains("ADMIN = true"));

        TextPage logoutPage = client.getPage(url + "logout");
        assertTrue(logoutPage.getContent().contains("SUCCESS"));

        TextPage afterLogout = client.getPage(url + "user");
        assertTrue(afterLogout.getContent().contains("USER = null"));
        assertTrue(afterLogout.getContent().contains("ADMIN = null"));

    }

    @Test
    public void testSeamSecurityFeatureAdminFlagOtherUser() throws Exception {

        WebClient client = new WebClient();

        TextPage beforeLogin = client.getPage(url + "user");
        assertTrue(beforeLogin.getContent().contains("USER = null"));
        assertTrue(beforeLogin.getContent().contains("ADMIN = null"));

        TextPage loginPage = client.getPage(url + "login?user=somebody");
        assertTrue(loginPage.getContent().contains("SUCCESS"));

        TextPage afterLogin = client.getPage(url + "user");
        assertTrue(afterLogin.getContent().contains("USER = somebody"));
        assertTrue(afterLogin.getContent().contains("ADMIN = false"));

        TextPage logoutPage = client.getPage(url + "logout");
        assertTrue(logoutPage.getContent().contains("SUCCESS"));

        TextPage afterLogout = client.getPage(url + "user");
        assertTrue(afterLogout.getContent().contains("USER = null"));
        assertTrue(afterLogout.getContent().contains("ADMIN = null"));

    }

    @Test
    public void testSeamSecurityWithCorrectUser() throws Exception {

        WebClient client = new WebClient();

        TextPage beforeLogin = client.getPage(url + "features");
        assertTrue(beforeLogin.getContent().contains("DISABLED = false"));
        assertTrue(beforeLogin.getContent().contains("ENABLED_FOR_ALL = true"));
        assertTrue(beforeLogin.getContent().contains("ENABLED_FOR_CK = false"));

        TextPage loginPage = client.getPage(url + "login?user=ck");
        assertTrue(loginPage.getContent().contains("SUCCESS"));

        TextPage afterLogin = client.getPage(url + "features");
        assertTrue(afterLogin.getContent().contains("DISABLED = false"));
        assertTrue(afterLogin.getContent().contains("ENABLED_FOR_ALL = true"));
        assertTrue(afterLogin.getContent().contains("ENABLED_FOR_CK = true"));

        TextPage logoutPage = client.getPage(url + "logout");
        assertTrue(logoutPage.getContent().contains("SUCCESS"));

        TextPage afterLogout = client.getPage(url + "features");
        assertTrue(afterLogout.getContent().contains("DISABLED = false"));
        assertTrue(afterLogout.getContent().contains("ENABLED_FOR_ALL = true"));
        assertTrue(afterLogout.getContent().contains("ENABLED_FOR_CK = false"));

    }

    @Test
    public void testSeamSecurityWithSomeOtherUser() throws Exception {

        WebClient client = new WebClient();

        TextPage beforeLogin = client.getPage(url + "features");
        assertTrue(beforeLogin.getContent().contains("DISABLED = false"));
        assertTrue(beforeLogin.getContent().contains("ENABLED_FOR_ALL = true"));
        assertTrue(beforeLogin.getContent().contains("ENABLED_FOR_CK = false"));

        TextPage loginPage = client.getPage(url + "login?user=somebody");
        assertTrue(loginPage.getContent().contains("SUCCESS"));

        TextPage afterLogin = client.getPage(url + "features");
        assertTrue(afterLogin.getContent().contains("DISABLED = false"));
        assertTrue(afterLogin.getContent().contains("ENABLED_FOR_ALL = true"));
        assertTrue(afterLogin.getContent().contains("ENABLED_FOR_CK = false"));

        TextPage logoutPage = client.getPage(url + "logout");
        assertTrue(logoutPage.getContent().contains("SUCCESS"));

        TextPage afterLogout = client.getPage(url + "features");
        assertTrue(afterLogout.getContent().contains("DISABLED = false"));
        assertTrue(afterLogout.getContent().contains("ENABLED_FOR_ALL = true"));
        assertTrue(afterLogout.getContent().contains("ENABLED_FOR_CK = false"));

    }

}
