package org.togglz.jsp;

import java.io.IOException;
import java.net.URL;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.test.Deployments;
import org.togglz.test.Packaging;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
public class JspTaglibTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.getBasicWebArchive()
            .addAsLibrary(Deployments.getTogglzJSPArchive())
            .addClass(JspTaglibFeature.class)
            .addClass(JspTaglibConfiguration.class)
            .addAsWebResource("jsp-taglib-test.jsp")
            .addAsWebResource("jsp-taglib-inverse-test.jsp")
            .setWebXML(Packaging.webAppDescriptor()
                .contextParam(TogglzConfig.class.getName(), JspTaglibConfiguration.class.getName())
                .exportAsAsset());

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

    @Test
    public void shouldIncludeOrExcludeBodyCorrectlyInverseCondition() throws IOException {
        WebClient client = new WebClient();
        HtmlPage page = client.getPage(url + "jsp-taglib-inverse-test.jsp");
        assertThat(page.asText())
                .contains("Feature [INACTIVE_FEATURE] is inactive")
                .doesNotContain("Feature [ACTIVE_FEATURE] is inactive");
    }

}
