package org.togglz.servlet.test.user.thread;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;

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
public class ThreadBasedUsersTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.getBasicWebArchive()
            .addClass(ThreadBasedUsersConfiguration.class)
            .addClass(ThreadBasedUsersFilter.class)
            .addClass(UserDependentFeature.class)
            .setWebXML(Packaging.webAppDescriptor()
                .contextParam(TogglzConfig.class.getName(), ThreadBasedUsersConfiguration.class.getName())
                .exportAsAsset());
    }

    @ArquillianResource
    private URL url;

    @Test
    public void testDisabledForAllUsers() throws IOException {
        WebClient client = new WebClient();
        TextPage page = client.getPage(url + "features?user=ck");
        assertTrue(page.getContent().contains("DISABLED = false"));
    }

    @Test
    public void testEnabledForAllUsers() throws IOException {
        WebClient client = new WebClient();
        TextPage page = client.getPage(url + "features?user=ck");
        assertTrue(page.getContent().contains("ENABLED_FOR_ALL = true"));
    }

    @Test
    public void testEnabledForOneUserWithCorrectUser() throws IOException {
        WebClient client = new WebClient();
        TextPage page = client.getPage(url + "features?user=ck");
        assertTrue(page.getContent().contains("ENABLED_FOR_CK = true"));
    }

    @Test
    public void testEnabledForOneUserWithOtherUsers() throws IOException {
        WebClient client = new WebClient();
        TextPage page = client.getPage(url + "features?user=other");
        assertTrue(page.getContent().contains("ENABLED_FOR_CK = false"));
    }

    @Test
    public void testFeatureAdminFlagForAdminUser() throws IOException {
        WebClient client = new WebClient();
        TextPage userPage = client.getPage(url + "user?user=ck");
        assertTrue(userPage.getContent().contains("USER = ck"));
        assertTrue(userPage.getContent().contains("ADMIN = true"));
    }

    @Test
    public void testFeatureAdminFlagForOtherUser() throws IOException {
        WebClient client = new WebClient();
        TextPage userPage = client.getPage(url + "user?user=other");
        assertTrue(userPage.getContent().contains("USER = other"));
        assertTrue(userPage.getContent().contains("ADMIN = false"));
    }

}
