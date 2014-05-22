package org.togglz.jsf.test.map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;

import javax.faces.webapp.FacesServlet;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.jsf.test.JSFFeatures;
import org.togglz.test.Deployments;
import org.togglz.test.Packaging;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@RunWith(Arquillian.class)
public class JSFMapTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.getBasicWebArchive()
            .addAsLibrary(Deployments.getTogglzJSFArchive())
            .addAsWebInfResource("faces-config.xml", "faces-config.xml")
            .addClass(JSFMapConfiguration.class)
            .addClass(JSFFeatures.class)
            .addAsWebResource("map-index.xhtml", "index.xhtml")
            .setWebXML(Packaging.webAppDescriptor()
                .contextParam(TogglzConfig.class.getName(), JSFMapConfiguration.class.getName())
                .servlet(FacesServlet.class, "*.jsf")
                .exportAsAsset());
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
