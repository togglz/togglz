package org.togglz.shiro.test;

import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.test.Deployments;
import org.togglz.test.Packaging;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;

@RunWith(Arquillian.class)
public class ShiroUsersTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.getBasicWebArchive()
            .addAsLibraries(Packaging.mavenDependencies()
                .artifact("org.apache.shiro:shiro-web")
                .asFiles())
            .addAsLibraries(Deployments.getTogglzShiroArchive())
            .addClass(ShiroUsersConfiguration.class)
            .addClass(ShiroLoginServlet.class)
            .addClass(ShiroLogoutServlet.class)
            .addClass(TestFeature.class)
            .addClass(ShiroTestRealm.class)
            .addAsWebInfResource("shiro.ini")
            .setWebXML(Packaging.webAppDescriptor()
                .contextParam(TogglzConfig.class.getName(), ShiroUsersConfiguration.class.getName())
                .listener(EnvironmentLoaderListener.class)
                .filter(ShiroFilter.class, "/*")
                .exportAsAsset());
    }

    @ArquillianResource
    private URL url;

    @Test
    public void testShiroAsAnonymousUser() throws Exception {

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
    public void testShiroLoginAsFeatureAdmin() throws Exception {

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
    public void testShiroLoginAsNormalUser() throws Exception {

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
    public void testShiroWithCorrectUser() throws Exception {

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
    public void testShiroWithSomeOtherUser() throws Exception {

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
