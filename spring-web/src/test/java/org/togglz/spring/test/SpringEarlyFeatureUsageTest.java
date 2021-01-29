package org.togglz.spring.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.togglz.test.Deployments;
import org.togglz.test.Packaging;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class SpringEarlyFeatureUsageTest {

    @Deployment
    public static WebArchive createDeployment() {
        return Deployments.getBasicWebArchive()
            .addAsLibrary(Deployments.getTogglzSpringArchive())
            .addAsLibraries(Packaging.mavenDependencies()
                .artifact("org.springframework:spring-web")
                .asFiles())
            .addAsWebInfResource("applicationContext.xml")
            .setWebXML("spring-web.xml")
            .addClass(SpringFeatureConfiguration.class)
            .addClass(BasicFeatures.class)
            .addClass(SpringEarlyFeatureUsageService.class);
    }

    @Test
    public void testEarlyFeatureUsage() {
        WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();

        assertNotNull(applicationContext);
        SpringEarlyFeatureUsageService service = applicationContext.getBean(SpringEarlyFeatureUsageService.class);

        assertFalse(service.isFeature1Active());
        assertFalse(service.isFeature2Active());
    }
}
