package org.togglz.guice;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;
import org.togglz.test.Deployments;
import org.togglz.test.Packaging;

import com.google.inject.servlet.GuiceFilter;

@RunWith(Arquillian.class)
public class GuiceIntegrationTest {

    @Deployment
    public static WebArchive createDeployment() {

        return Deployments.getBasicWebArchive()
            .addAsLibrary(Deployments.getTogglzGuiceArchive())
            .addAsLibraries(Packaging.mavenDependencies()
                    .artifact("com.google.inject:guice")
                    .artifact("com.google.inject.extensions:guice-servlet")
                    .asFiles())
            .addPackage(GuiceIntegrationTest.class.getPackage())
            .addPackages(true, "org.assertj")
            .setWebXML(Packaging.webAppDescriptor()
                    .filter(GuiceFilter.class, "/*")
                    .listener(SimpleGuiceServletListener.class)
                    .exportAsAsset());

    }

    @Test
    public void testGuiceIntegration() {

        FeatureManager featureManager = FeatureContext.getFeatureManagerOrNull();

        assertThat(featureManager).isNotNull();
        assertThat(featureManager.getFeatures())
            .containsExactly(GuiceFeatures.FEATURE1, GuiceFeatures.FEATURE2);

    }

}
