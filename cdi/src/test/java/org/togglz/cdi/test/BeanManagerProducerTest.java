package org.togglz.cdi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.test.Deployments;

@RunWith(Arquillian.class)
public class BeanManagerProducerTest {

    @Deployment
    public static WebArchive createDeployment() {
        return Deployments.getBasicWebArchive()
                .addAsLibrary(Deployments.getTogglzCDIArchive())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClass(CDIFeatureConfiguration.class)
                .addClass(BasicFeatures.class);
    }

    @Inject
    private FeatureManager featureManager;

    @Test
    public void testFeatureManagerInjection() throws IOException {

        assertNotNull(featureManager);
        List<Feature> features = new ArrayList<Feature>(featureManager.getFeatures());
        assertEquals(2, features.size());
        assertEquals(BasicFeatures.FEATURE1, features.get(0));
        assertEquals(BasicFeatures.FEATURE2, features.get(1));

    }

}
