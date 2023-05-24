package org.togglz.spring.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.UserProvider;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(classes = TogglzTestExecutionListenerTest.TestTogglzFeatureManagerConfig.class)
@TestExecutionListeners(
        value = {
                DependencyInjectionTestExecutionListener.class,
                DirtiesContextTestExecutionListener.class,
                TogglzTestExecutionListener.class
        },
        inheritListeners = true
)
class TogglzTestExecutionListenerTest {

    @Component
    static class TestTogglzConfig implements TogglzConfig {
        @Override
        public Class<? extends Feature> getFeatureClass() {
            return TestFeatures.class;
        }

        @Override
        public StateRepository getStateRepository() {
            return new InMemoryStateRepository();
        }

        @Override
        public UserProvider getUserProvider() {
            return new NoOpUserProvider();
        }
    }

    @Configuration
    @Import(TestTogglzConfig.class)
    static class TestTogglzFeatureManagerConfig {
        @Bean
        public FeatureManager featureManager(TogglzConfig togglzConfig) {
            return FeatureManagerBuilder.begin()
                    .togglzConfig(togglzConfig)
                    .build();
        }
    }

    static enum TestFeatures implements Feature {
        TEST_FEATURES;
    }

    @Autowired
    FeatureManager featureManager;

    @Test
    public void testFeatureManagerExists() {
        assertNotNull(featureManager);

        featureManager.enable(TestFeatures.TEST_FEATURES);
        assertTrue(featureManager.getFeatureState(TestFeatures.TEST_FEATURES).isEnabled());
    }

}
