package org.togglz.core.context;

import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ContextClassLoaderFeatureManagerProviderTest {

    @Test
    void shouldThrowIllegalStateExceptionWhenTryingToBindFeatureManagerTwice() {
        MyFeatureManager manager = new MyFeatureManager();
        ContextClassLoaderFeatureManagerProvider.bind(manager);

        assertThrows(IllegalStateException.class, () -> ContextClassLoaderFeatureManagerProvider.bind(manager));
    }

    static class MyFeatureManager implements FeatureManager {

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Set<Feature> getFeatures() {
            return null;
        }

        @Override
        public FeatureMetaData getMetaData(Feature feature) {
            return null;
        }

        @Override
        public boolean isActive(Feature feature) {
            return false;
        }

        @Override
        public FeatureUser getCurrentFeatureUser() {
            return null;
        }

        @Override
        public FeatureState getFeatureState(Feature feature) {
            return null;
        }

        @Override
        public void setFeatureState(FeatureState state) {

        }

        @Override
        public List<ActivationStrategy> getActivationStrategies() {
            return null;
        }
    }
}