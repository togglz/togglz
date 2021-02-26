package org.togglz.redis;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.util.NamedFeature;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.junit.jupiter.api.Assertions.*;

class RedisStateRepositoryTest {

    @Container
    public GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:6.2.0-alpine"))
            .withExposedPorts(6379)
            .withReuse(true);

    @BeforeEach
    void before() {
        redis.start();
    }

    @AfterEach
    void after() {
        redis.stop();
    }

    @Test
    void getFeatureStateNotExisting() {
        final StateRepository stateRepository = aRedisStateRepository();
        final Feature feature = new NamedFeature("A_FEATURE");

        stateRepository.getFeatureState(feature);
        final FeatureState storedFeatureState = stateRepository.getFeatureState(feature);

        assertNull(storedFeatureState);
    }

    @Test
    void setFeatureStateWithStrategyAndParameter() {
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
    void setFeatureStateExisting() {
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
    void formatOfExistingFeatureState() {

        // set contents in Redis directly, without using the RedisStateRepository API
        final JedisPool jedisPool = new JedisPool(redis.getContainerIpAddress(), redis.getMappedPort(6379));
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
        return new RedisStateRepository.Builder().jedisPool(new JedisPool(redis.getContainerIpAddress(), redis.getMappedPort(6379))).keyPrefix("feature-toggles:").build();
    }
}
