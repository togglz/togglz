package de.chkal.togglz.test.basic.cdi;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;

import de.chkal.togglz.test.Deployments;
import de.chkal.togglz.test.basic.BasicFeatures;

@RunWith(Arquillian.class)
public class CDIBasicOperationTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.getCDIArchive()
                .addClass(CDIFeatureConfiguration.class)
                .addClass(BasicFeatures.class);
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
