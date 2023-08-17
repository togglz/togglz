package org.togglz.spring.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;

import org.htmlunit.TextPage;
import org.htmlunit.WebClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.test.Deployments;
import org.togglz.test.Packaging;

@RunWith(Arquillian.class)
public class SpringBasicOperationTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.getBasicWebArchive()
            .addAsLibrary(Deployments.getTogglzSpringArchive())
            .addAsLibraries(Packaging.mavenDependencies()
                .artifact("org.springframework:spring-web")
                .artifact("org.springframework:spring-context")
                .asFiles())
            .addAsWebInfResource("applicationContext.xml")
            .setWebXML("spring-web.xml")
            .addClass(SpringFeatureConfiguration.class)
            .addClass(BasicFeatures.class);
    }

    @ArquillianResource
    private URL url;

    @Test
    public void testSpringBasicFeatures() throws IOException {
        TextPage page;
        try (WebClient client = new WebClient()) {
            page = client.getPage(url + "features");
        }
        assertTrue(page.getContent().contains("FEATURE1 = false"));
        assertTrue(page.getContent().contains("FEATURE2 = true"));
    }

}
