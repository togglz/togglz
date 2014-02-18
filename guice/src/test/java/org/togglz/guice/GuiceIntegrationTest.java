package org.togglz.guice;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

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
import org.togglz.core.manager.FeatureManager;
import org.togglz.test.Deployments;

import com.google.inject.servlet.GuiceFilter;

@RunWith(Arquillian.class)
public class GuiceIntegrationTest {

    @Deployment
    public static WebArchive createDeployment() {

        return Deployments.getBasicWebArchive()
            .addAsLibrary(Deployments.getTogglzGuiceArchive())
            .addAsLibraries(
                DependencyResolvers.use(MavenDependencyResolver.class)
                    .artifact("com.google.inject:guice:3.0")
                    .artifact("com.google.inject.extensions:guice-servlet:3.0")
                    .resolveAs(JavaArchive.class))
            .addPackage(GuiceIntegrationTest.class.getPackage())
            .addPackages(true, "org.fest")
            .setWebXML(new StringAsset(
                Descriptors.create(WebAppDescriptor.class)
                    .filter(GuiceFilter.class, "/*")
                    .listener(SimpleGuiceServletListener.class)
                    .exportAsString()));

    }

    @Test
    public void testGuiceIntegration() throws IOException {

        FeatureManager featureManager = FeatureContext.getFeatureManagerOrNull();

        assertThat(featureManager).isNotNull();
        assertThat(featureManager.getFeatures())
            .containsExactly(GuiceFeatures.FEATURE1, GuiceFeatures.FEATURE2);

    }

}
