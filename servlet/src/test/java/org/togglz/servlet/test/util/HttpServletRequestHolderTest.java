package org.togglz.servlet.test.util;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.test.Deployments;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;

@RunWith(Arquillian.class)
public class HttpServletRequestHolderTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.getBasicWebArchive()
            .addClass(HttpServletRequestHolderServlet.class)
            // we don't need to bootstrap Togglz here as we only test the request holder
            .setWebXML(new StringAsset(
                Descriptors.create(WebAppDescriptor.class)
                    .contextParam("org.togglz.LOCAL_FEATURE_MANAGER", "false")
                    .exportAsString()));

    }

    @ArquillianResource
    private URL baseUrl;

    @Test
    public void testRequestIsBoundToHolder() throws IOException {

        // send a request to the servlet with a query string part
        String url = baseUrl + HttpServletRequestHolderServlet.URL_PATTERN + "?number=42";
        TextPage page = new WebClient().getPage(url);

        // verify the servlet sends back the query string
        assertThat(page.getWebResponse().getStatusCode()).isEqualTo(200);
        assertThat(page.getContent()).contains("number=42");

    }

}
