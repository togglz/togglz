package org.togglz.servlet.test.repository.cache;

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
public class CacheRepositoryTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.getBasicWebArchive()
            .addClass(CacheRepositoryConfiguration.class)
            .addClass(SlowStateRepository.class)
            .addClass(CacheFeatures.class)
            .setWebXML(Packaging.webAppDescriptor()
                .contextParam(TogglzConfig.class.getName(), CacheRepositoryConfiguration.class.getName())
                .exportAsAsset());
    }

    @ArquillianResource
    private URL url;

    @Test
    public void testCachingOfFeatureState() throws IOException {

        WebClient client = new WebClient();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            TextPage page = client.getPage(url + "features?user=ck");
            assertTrue(page.getContent().contains("F1 = false"));
        }
        long duration = System.currentTimeMillis() - start;

        assertTrue(duration < 4000); // without cache: 500ms * 10 = 5000ms

    }
}
