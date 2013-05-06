package org.togglz.jsp;

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
import org.togglz.core.manager.TogglzConfig;
import org.togglz.test.Deployments;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@RunWith(Arquillian.class)
public class JspTaglibTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.getBasicWebArchive()
            .addAsLibrary(Deployments.getTogglzJSPArchive())
            .addClass(JspTaglibFeature.class)
            .addClass(JspTaglibConfiguration.class)
            .addAsWebResource("jsp-taglib-test.jsp")
            .setWebXML(new StringAsset(
                Descriptors.create(WebAppDescriptor.class)
                    .contextParam(TogglzConfig.class.getName(), JspTaglibConfiguration.class.getName())
                    .exportAsString()));

    }

    @ArquillianResource
    private URL url;

    @Test
    public void shouldIncludeOrExcludeBodyCorrectly() throws IOException {
        WebClient client = new WebClient();
        HtmlPage page = client.getPage(url + "jsp-taglib-test.jsp");
        assertThat(page.asText())
            .contains("Feature [ACTIVE_FEATURE] is active")
            .doesNotContain("Feature [INACTIVE_FEATURE] is active");
    }

}