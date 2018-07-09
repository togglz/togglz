package org.togglz.spring.test.container;

import static org.junit.Assert.assertEquals;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.core.context.FeatureContext;
import org.togglz.spring.test.BasicFeatures;
import org.togglz.test.Deployments;
import org.togglz.test.Packaging;

@RunWith(Arquillian.class)
public class ManagedFeatureManagerTest {

    @Deployment
    public static WebArchive createDeployment() {
        return Deployments.getBasicWebArchive()
            .addAsLibrary(Deployments.getTogglzSpringArchive())
            .addAsLibraries(Packaging.mavenDependencies()
                    .artifact("org.springframework:spring-web")
                    .asFiles())
            .addClass(BasicFeatures.class)
            .addAsWebInfResource("applicationContext-container.xml")
            .setWebXML(Packaging.webAppDescriptor()
                    .contextParam("org.togglz.FEATURE_MANAGER_PROVIDED", "true")
                    .contextParam("contextConfigLocation", "/WEB-INF/applicationContext*.xml")
                    .listener("org.springframework.web.context.ContextLoaderListener")
                    .exportAsAsset());
    }

    @Test
    public void containerManagedFeatureManagerShouldBeUsedIfAvailable() {
        assertEquals("I'm managed by Spring",
            FeatureContext.getFeatureManager().getName());
    }

}
