package org.togglz.ws.server;

import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.context.ThreadLocalFeatureManagerProvider;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.UserProvider;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ContextTogglzWebServiceTest {

    @Test
    public void canDetermineIfFeatureIsActive() {
        ThreadLocalFeatureManagerProvider.bind(new FeatureManagerBuilder().togglzConfig(new MyConfig()).build());
        TogglzWebService service = new ContextTogglzWebService();
        assertTrue(service.isFeatureActive(MyFeatures.ONE.name()));
        assertFalse(service.isFeatureActive(MyFeatures.TWO.name()));
    }

    class MyConfig implements TogglzConfig {

        public Class<? extends Feature> getFeatureClass() {
            return MyFeatures.class;
        }

        public StateRepository getStateRepository() {
            return new InMemoryStateRepository();
        }

        public UserProvider getUserProvider() {
            return new NoOpUserProvider();
        }

    }
    
    enum MyFeatures implements Feature {

        @EnabledByDefault
        @Label("Feature 1")
        ONE,

        @Label("Feature 2")
        TWO;

        public boolean isActive() {
            return FeatureContext.getFeatureManager().isActive(this);
        }

    }

}
