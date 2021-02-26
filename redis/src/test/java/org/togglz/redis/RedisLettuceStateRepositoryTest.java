package org.togglz.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
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

import static org.junit.jupiter.api.Assertions.*;

public class RedisLettuceStateRepositoryTest {

    @Container
    public GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:6.2.0-alpine"))
            .withExposedPorts(6379)
            .withReuse(true);

    @BeforeEach
    public void before() {
        redis.start();
    }

    @AfterEach
    public void after() {
        redis.stop();
    }

    @Test
    public void testGetFeatureStateNotExisting() {
        final StateRepository stateRepository = aRedisStateRepository();
        final Feature feature = new NamedFeature("A_FEATURE");

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
    public void testFormatOfExistingFeatureState() throws Exception {

        // set contents in Redis directly, without using the RedisStateRepository API
        final GenericObjectPool<StatefulConnection<String, String>> lettucePool = createPool();
        try (final StatefulRedisConnection<String, String> connection = (StatefulRedisConnection<String, String>) lettucePool.borrowObject()) {
            final String key = "feature-toggles:A_FEATURE";
            connection.sync().hset(key, "enabled", "true");
            connection.sync().hset(key, "strategy", "TIT_FOR_TAT");
            connection.sync().hset(key, "parameter:MEANING_OF_LIFE", "42");
        }

        final Feature feature = new NamedFeature("A_FEATURE");
        final FeatureState expectedFeatureState = new FeatureState(feature, true);
        expectedFeatureState.setStrategyId("TIT_FOR_TAT");
        expectedFeatureState.setParameter("MEANING_OF_LIFE", "42");

        final FeatureState storedFeatureState = aRedisStateRepository().getFeatureState(feature);

        assertTrue(EqualsBuilder.reflectionEquals(expectedFeatureState, storedFeatureState, true));
    }

    private RedisLettuceStateRepository aRedisStateRepository() {
        return new RedisLettuceStateRepository.Builder()
            .keyPrefix("feature-toggles:")
            .lettucePool(createPool())
            .build();
    }

    private GenericObjectPool<StatefulConnection<String, String>> createPool() {
        RedisClient client = RedisClient.create(RedisURI.create(redis.getContainerIpAddress(), redis.getMappedPort(6379)));
        return ConnectionPoolSupport
            .createGenericObjectPool(() -> client.connect(), new GenericObjectPoolConfig());
    }
 }
