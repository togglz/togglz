package de.chkal.togglz.test.basic.cdi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.chkal.togglz.core.manager.FeatureManager;
import de.chkal.togglz.test.Deployments;
import de.chkal.togglz.test.basic.BasicFeatures;

@RunWith(Arquillian.class)
public class BeanManagerProducerTest {

    @Deployment
    public static WebArchive createDeployment() {
        return Deployments.getCDIArchive()
                .addClass(CDIFeatureConfiguration.class)
                .addClass(BasicFeatures.class);
    }

    @Inject
    private FeatureManager featureManager;

    @Test
    public void testFeatureManagerInjection() throws IOException {

        assertNotNull(featureManager);
        assertEquals(2, featureManager.getFeatures().length);
        assertEquals(BasicFeatures.FEATURE1, featureManager.getFeatures()[0]);
        assertEquals(BasicFeatures.FEATURE2, featureManager.getFeatures()[1]);

    }

}
