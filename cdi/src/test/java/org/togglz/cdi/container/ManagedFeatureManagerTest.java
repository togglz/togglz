package org.togglz.cdi.container;

import static org.junit.Assert.assertEquals;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.cdi.Features;
import org.togglz.core.context.FeatureContext;
import org.togglz.test.Deployments;
import org.togglz.test.Packaging;

@RunWith(Arquillian.class)
public class ManagedFeatureManagerTest {

    @Deployment
    public static WebArchive createDeployment() {
        return Deployments.getBasicWebArchive()
            .addAsLibrary(Deployments.getTogglzCDIArchive())
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addClass(Features.class)
            .addClass(ManagedFeatureManagerProvider.class)
            .setWebXML(Packaging.webAppDescriptor()
                    .contextParam("org.togglz.FEATURE_MANAGER_PROVIDED", "true")
                    .exportAsAsset());
    }

    @Test
    public void containerManagedFeatureManagerShouldBeUsedIfAvailable() {
        assertEquals("I'm managed by CDI",
            FeatureContext.getFeatureManager().getName());
    }

}
