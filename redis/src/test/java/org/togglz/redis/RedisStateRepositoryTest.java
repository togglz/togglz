package org.togglz.redis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.util.DefaultMapSerializer;
import org.togglz.core.util.NamedFeature;
import redis.clients.jedis.Protocol;
import redis.embedded.RedisServer;

public class RedisStateRepositoryTest {

    private RedisServer redisServer;

    @Before
    public void before() throws IOException {
        redisServer = new RedisServer();
        redisServer.start();
    }

    @After
    public void after() {
        redisServer.stop();
    }

    @Test
    public void testGetFeatureStateNotExisting() {
        final StateRepository stateRepository = new RedisStateRepository.Builder().
                hostname(Protocol.DEFAULT_HOST).
                config(null).
                mapSerializer(DefaultMapSerializer.singleline()).
                build();
        final Feature feature = new NamedFeature("A_FEATURE");

        stateRepository.getFeatureState(feature);
        final FeatureState storedFeatureState = stateRepository.getFeatureState(feature);

        assertNull(storedFeatureState);
    }

    @Test
    public void testSetFeatureStateWithStrategyAndParameter() {
        final StateRepository stateRepository = new RedisStateRepository();
        final Feature feature = new NamedFeature("A_FEATURE");
        final FeatureState featureState = new FeatureState(feature, true);
        featureState.setStrategyId("TIT_FOR_TAT");
        featureState.setParameter("MEANING_OF_LIFE", "42");

        stateRepository.setFeatureState(featureState);
        final FeatureState storedFeatureState = stateRepository.getFeatureState(feature);

        assertTrue(EqualsBuilder.reflectionEquals(featureState, storedFeatureState, true));
    }

    @Test
    public void testSetFeatureStateExisting() {
        final StateRepository stateRepository = new RedisStateRepository();
        final Feature feature = new NamedFeature("A_FEATURE");
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
}
