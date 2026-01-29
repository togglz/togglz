package org.togglz.hazelcast;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import java.time.Duration;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.util.NamedFeature;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HazelcastStateRepositoryTest {

    @Test
    void testSetFeatureStateNotExistingInMap() {
        final StateRepository stateRepository = HazelcastStateRepository.newBuilder().mapName("togglzMap").build();
        final Feature feature = new NamedFeature("SAMPLE_FEATURE");
        final FeatureState featureState = new FeatureState(feature, true);
        stateRepository.setFeatureState(featureState);

        final FeatureState storedFeatureState = stateRepository.getFeatureState(feature);

        assertTrue(EqualsBuilder.reflectionEquals(featureState, storedFeatureState, true));
    }

    @Test
    void testSetFeatureStateExistingInMap() {
        final StateRepository stateRepository = HazelcastStateRepository.newBuilder().mapName("togglzMap").build();
        final Feature feature = new NamedFeature("SAMPLE_FEATURE");
        final FeatureState featureState = new FeatureState(feature, true);
        stateRepository.setFeatureState(featureState);

        FeatureState storedFeatureState = stateRepository.getFeatureState(feature);
        assertTrue(storedFeatureState.isEnabled());
        assertTrue(EqualsBuilder.reflectionEquals(featureState, storedFeatureState, true));

        featureState.setEnabled(false);
        stateRepository.setFeatureState(featureState);
        storedFeatureState = stateRepository.getFeatureState(feature);
        assertFalse(storedFeatureState.isEnabled());

        assertTrue(EqualsBuilder.reflectionEquals(featureState, storedFeatureState, true));
    }

    @Test
        // may fail if you are connected to a VPN
    void test2nodeSetup() {
        final Config config = new Config();
        final HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        final StateRepository stateRepository1 = HazelcastStateRepository.newBuilder().mapName("togglzMap").hazelcastInstance(hazelcastInstance).build();
        final StateRepository stateRepository2 = HazelcastStateRepository.newBuilder().mapName("togglzMap").hazelcastInstance(hazelcastInstance).build();
        final Feature feature = new NamedFeature("SAMPLE_FEATURE");
        final FeatureState featureState = new FeatureState(feature, false);
        stateRepository1.setFeatureState(featureState);

        assertFalse(stateRepository1.getFeatureState(feature).isEnabled());
        //Wait until the second node has received the update
        Awaitility.waitAtMost(Duration.ofSeconds(1)).until(() -> stateRepository2.getFeatureState(feature) != null);
        assertFalse(stateRepository2.getFeatureState(feature).isEnabled());

        featureState.setEnabled(true);
        stateRepository1.setFeatureState(featureState);

        assertTrue(stateRepository1.getFeatureState(feature).isEnabled());
        //Wait until the second node has received the update
        Awaitility.waitAtMost(Duration.ofSeconds(1)).until(() -> stateRepository2.getFeatureState(feature).isEnabled());
    }

    @Test
    void onlyConfigSetBuildsSuccessfully() {
        HazelcastStateRepository stateRepository = HazelcastStateRepository.newBuilder()
                .config(new Config())
                .build();

        assertNotNull(stateRepository);
    }

    @Test
    void onlyClientConfigSetBuildsSuccessfully() {
        HazelcastStateRepository stateRepository = HazelcastStateRepository.newBuilder()
                .clientConfig(new ClientConfig())
                .build();

        assertNotNull(stateRepository);
    }

    @Test
    void onlyHazelcastInstanceSetBuildsSuccessfully() {
        HazelcastStateRepository stateRepository = HazelcastStateRepository.newBuilder()
                .hazelcastInstance(Hazelcast.newHazelcastInstance())
                .build();

        assertNotNull(stateRepository);
    }

    @Test
    void multipleConfiguredPiecesThrowsIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> HazelcastStateRepository.newBuilder()
                .clientConfig(new ClientConfig())
                .config(new Config())
                .build());
    }
}
