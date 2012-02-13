package org.togglz.test.basic.spring;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.togglz.test.Deployments;
import org.togglz.test.basic.BasicFeatures;


@RunWith(Arquillian.class)
public class SpringEarlyFeatureUsageTest {

    @Deployment
    public static WebArchive createDeployment() {
        return Deployments.getSpringArchive()
                .addClass(SpringFeatureConfiguration.class)
                .addClass(BasicFeatures.class)
                .addClass(SpringEarlyFeatureUsageService.class);
    }

    @Test
    public void testEarlyFeatureUsage() throws IOException {

        WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
        SpringEarlyFeatureUsageService service = applicationContext.getBean(SpringEarlyFeatureUsageService.class);

        assertEquals(false, service.isFeature1Active());
        assertEquals(false, service.isFeature2Active());

    }

}
