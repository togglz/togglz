package org.togglz.spring.test.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.FeatureState;
import org.togglz.test.Deployments;
import org.togglz.test.Packaging;

@RunWith(Arquillian.class)
public class FeatureProxyTest {

    @Deployment
    public static WebArchive createDeployment() {
        return Deployments.getBasicWebArchive()
            .addAsLibrary(Deployments.getTogglzSpringArchive())
            .addAsLibraries(Packaging.mavenDependencies()
                .artifact("org.springframework:spring-web")
                .asFiles())
            .addAsWebInfResource("applicationContext.xml")
            .addAsWebInfResource("applicationContext-proxy.xml")
            .setWebXML("spring-web.xml")
            .addClass(ProxyFeatures.class)
            .addClass(FeatureProxyConfiguration.class)
            .addClass(SomeService.class)
            .addClass(SomeServiceActive.class)
            .addClass(SomeServiceInactive.class);
    }

    @Test
    public void testProxyWithManuallySetProxyType() throws IOException {

        // obtain the service
        SomeService someService = (SomeService) getSpringBean("someServiceManuallySetProxyType");
        assertNotNull(someService);

        // disable the feature flag
        FeatureContext.getFeatureManager().setFeatureState(new FeatureState(ProxyFeatures.SERVICE_TOGGLE, false));

        // first the inactive Service is invoked
        assertEquals("I'm SomeServiceInactive", someService.whoAreYou());

        // enable the feature flag
        FeatureContext.getFeatureManager().setFeatureState(new FeatureState(ProxyFeatures.SERVICE_TOGGLE, true));

        // calls are now delegated to the other service implementation
        assertEquals("I'm SomeServiceActive", someService.whoAreYou());

    }

    @Test
    public void testProxyWithAutoDetectedProxyType() throws IOException {

        // obtain the service
        SomeService someService = (SomeService) getSpringBean("someServiceAutoDetectProxyType");
        assertNotNull(someService);

        // disable the feature flag
        FeatureContext.getFeatureManager().setFeatureState(new FeatureState(ProxyFeatures.SERVICE_TOGGLE, false));

        // first the inactive Service is invoked
        assertEquals("I'm SomeServiceInactive", someService.whoAreYou());

        // enable the feature flag
        FeatureContext.getFeatureManager().setFeatureState(new FeatureState(ProxyFeatures.SERVICE_TOGGLE, true));

        // calls are now delegated to the other service implementation
        assertEquals("I'm SomeServiceActive", someService.whoAreYou());

    }

    private Object getSpringBean(String name) {
        WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
        return applicationContext.getBean(name);
    }

}
