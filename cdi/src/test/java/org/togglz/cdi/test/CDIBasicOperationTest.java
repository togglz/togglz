package org.togglz.cdi.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.cdi.Features;
import org.togglz.test.Deployments;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;

@RunWith(Arquillian.class)
public class CDIBasicOperationTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.getBasicWebArchive()
                .addAsLibrary(Deployments.getTogglzCDIArchive())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClass(CDIFeatureConfiguration.class)
                .addClass(Features.class);
    }

    @ArquillianResource
    private URL url;

    @Test
    public void testCDIBasicFeatures() throws IOException {

        WebClient client = new WebClient();
        TextPage page = client.getPage(url + "features");
        assertTrue(page.getContent().contains("FEATURE1 = false"));
        assertTrue(page.getContent().contains("FEATURE2 = true"));

    }

}
