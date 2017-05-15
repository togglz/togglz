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
import org.togglz.core.util.NamedFeature;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
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
        final StateRepository stateRepository = aRedisStateRepository();
        final Feature feature = new NamedFeature("A_FEATURE");

        stateRepository.getFeatureState(feature);
        final FeatureState storedFeatureState = stateRepository.getFeatureState(feature);

        assertNull(storedFeatureState);
    }

    @Test
    public void testSetFeatureStateWithStrategyAndParameter() {
        final StateRepository stateRepository = aRedisStateRepository();
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
        final StateRepository stateRepository = aRedisStateRepository();
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

    @Test
    public void testFormatOfExistingFeatureState() {

        // set contents in Redis directly, without using the RedisStateRepository API
        final JedisPool jedisPool = new JedisPool();
        try (final Jedis jedis = jedisPool.getResource()) {
            final String key = "feature-toggles:A_FEATURE";
            jedis.hset(key, "enabled", "true");
            jedis.hset(key, "strategy", "TIT_FOR_TAT");
            jedis.hset(key, "parameter:MEANING_OF_LIFE", "42");
        }

        final Feature feature = new NamedFeature("A_FEATURE");
        final FeatureState expectedFeatureState = new FeatureState(feature, true);
        expectedFeatureState.setStrategyId("TIT_FOR_TAT");
        expectedFeatureState.setParameter("MEANING_OF_LIFE", "42");

        final FeatureState storedFeatureState = aRedisStateRepository().getFeatureState(feature);

        assertTrue(EqualsBuilder.reflectionEquals(expectedFeatureState, storedFeatureState, true));
    }

    private RedisStateRepository aRedisStateRepository() {
        return new RedisStateRepository.Builder().keyPrefix("feature-toggles:").build();
    }
}
