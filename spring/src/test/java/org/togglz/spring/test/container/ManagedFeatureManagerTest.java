package org.togglz.spring.test.container;

import static org.junit.Assert.assertEquals;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.core.context.FeatureContext;
import org.togglz.spring.test.BasicFeatures;
import org.togglz.test.Deployments;

@RunWith(Arquillian.class)
public class ManagedFeatureManagerTest {

    @Deployment
    public static WebArchive createDeployment() {
        return Deployments.getBasicWebArchive()
            .addAsLibrary(Deployments.getTogglzSpringArchive())
            .addAsLibraries(
                DependencyResolvers.use(MavenDependencyResolver.class)
                    .artifact("org.springframework:spring-web:3.0.7.RELEASE")
                    .resolveAs(JavaArchive.class))
            .addClass(BasicFeatures.class)
            .addAsWebInfResource("applicationContext-container.xml")
            .setWebXML(new StringAsset(
                Descriptors.create(WebAppDescriptor.class)
                    .contextParam("org.togglz.FEATURE_MANAGER_PROVIDED", "true")
                    .contextParam("contextConfigLocation", "/WEB-INF/applicationContext*.xml")
                    .listener("org.springframework.web.context.ContextLoaderListener")
                    .exportAsString()));
    }

    @Test
    public void containerManagedFeatureManagerShouldBeUsedIfAvailable() {
        assertEquals("I'm managed by Spring",
            FeatureContext.getFeatureManager().getName());
    }

}
