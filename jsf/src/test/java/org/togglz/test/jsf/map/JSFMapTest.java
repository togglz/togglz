package org.togglz.test.jsf.map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.test.Deployments;
import org.togglz.test.jsf.JSFFeatures;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


@RunWith(Arquillian.class)
public class JSFMapTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.getJSFArchive()
                .addClass(JSFMapConfiguration.class)
                .addClass(JSFFeatures.class)
                .addAsWebResource("jsf/map/map-index.xhtml", "index.xhtml");
    }

    @ArquillianResource
    private URL url;

    @Test
    public void testJSFFeatureMap() throws IOException, Exception {

        WebClient client = new WebClient();
        HtmlPage page = client.getPage(url + "index.jsf");

        // this part of the page is rendered
        assertTrue(page.asText().contains("Text for ENABLED feature!"));

        // this part is disabled
        assertFalse(page.asText().contains("Text for DISABLED feature!"));

        // one div can be found the other not
        assertNotNull(page.getElementById("enabledDiv"));
        assertNull(page.getElementById("disabledDiv"));

    }

}
