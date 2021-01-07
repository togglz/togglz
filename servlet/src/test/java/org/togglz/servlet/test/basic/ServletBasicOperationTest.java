package org.togglz.servlet.test.basic;

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
public class ServletBasicOperationTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.getBasicWebArchive()
            .addClass(ServletFeatureConfiguration.class)
            .addClass(BasicFeatures.class)
            .setWebXML(Packaging.webAppDescriptor()
                .contextParam(TogglzConfig.class.getName(), ServletFeatureConfiguration.class.getName())
                .exportAsAsset()
            );
    }

    @ArquillianResource
    private URL url;

    @Test
    public void testServletBasicFeatures() throws IOException {

        WebClient client = new WebClient();
        TextPage page = client.getPage(url + "features");
        assertTrue(page.getContent().contains("FEATURE1 = false"));
        assertTrue(page.getContent().contains("FEATURE2 = true"));

    }

}
