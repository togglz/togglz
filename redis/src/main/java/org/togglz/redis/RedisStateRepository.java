package org.togglz.redis;

import java.util.Map;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.util.DefaultMapSerializer;
import org.togglz.core.repository.util.MapSerializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * <p>
 * A state repository which stores the feature state in Redis.
 * </p>
 * <p>
 * <p>
 * The class provides a builder which can be used to configure the Redis instance.
 * </p>
 * <p>
 *
 * @author Cosmin Rentea
 */
public class RedisStateRepository implements StateRepository {

    public static final String PREFIX = "togglz-";
    protected final JedisPool jedisPool;
    protected final JedisPoolConfig jedisPoolConfig;
    protected final String hostname;

    public RedisStateRepository(JedisPoolConfig jedisPoolConfig, String hostname) {
        this.jedisPoolConfig = jedisPoolConfig;
        this.hostname = hostname;
        jedisPool = createJedisPool();
    }

    public RedisStateRepository(JedisPoolConfig jedisPoolConfig) {
        this.jedisPoolConfig = jedisPoolConfig;
        this.hostname = Protocol.DEFAULT_HOST;
        jedisPool = createJedisPool();
    }

    public RedisStateRepository() {
        this.jedisPoolConfig = null;
        this.hostname = Protocol.DEFAULT_HOST;
        jedisPool = createJedisPool();
    }

    private RedisStateRepository(Builder builder) {
        jedisPoolConfig = builder.jedisPoolConfig;
        hostname = builder.hostname;
        jedisPool = createJedisPool();
    }

    private JedisPool createJedisPool() {
        return jedisPoolConfig != null ?
                new JedisPool(jedisPoolConfig, hostname) :
                new JedisPool();
    }

    @Override
    public FeatureState getFeatureState(final Feature feature) {
        try (Jedis jedis = jedisPool.getResource()) {
            final Map<String, String> map = jedis.hgetAll(PREFIX + feature.name());
            if (map == null || map.size() == 0) {
                return null;
            }
            final FeatureState featureState = new FeatureState(feature);
            //TODO fill-in fields of featureState with values from map
            return featureState;
        }
    }

    @Override
    public void setFeatureState(final FeatureState featureState) {
        try (Jedis jedis = jedisPool.getResource()) {
            //TODO extract const for prefix
            final String featureKey = PREFIX +featureState.getFeature().name();
            //TODO extract constants for keys
            //TODO optimize boolean/string code
            jedis.hset(featureKey, "enabled", featureState.isEnabled() ? "true": "false");
            jedis.hset(featureKey, "strategy", featureState.getStrategyId());
            //TODO extract mapSerializer as field
            final MapSerializer mapSerializer = DefaultMapSerializer.multiline();
            jedis.hset(featureKey, "parameters", mapSerializer.serialize(featureState.getParameterMap()));
        }
    }

    /**
     * Creates a new builder for creating a {@link RedisStateRepository}.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Creates a new builder for creating a {@link RedisStateRepository}.
     *
     * @param hostname the Hazelcast map name
     */
    public static Builder newBuilder(String hostname) {
        return new Builder(hostname);
    }

    /**
     * Builder for a {@link RedisStateRepository}.
     */
    public static class Builder {

        private String hostname = Protocol.DEFAULT_HOST;
        private JedisPoolConfig jedisPoolConfig = null;

        /**
         * Creates a new builder for a {@link RedisStateRepository}.
         */
        public Builder() {
        }

        /**
         * Creates a new builder for a {@link RedisStateRepository}.
         *
         * @param hostname the Hazelcast map name to use for feature state store
         */
        public Builder(String hostname) {
            this.hostname = hostname;
        }

        /**
         * Creates a new builder for a {@link RedisStateRepository}.
         *
         * @param jedisPoolConfig the Jedis pool configuration {@link JedisPoolConfig}
         */
        public Builder(JedisPoolConfig jedisPoolConfig) {
            this.jedisPoolConfig = jedisPoolConfig;
        }

        /**
         * Sets the Hazelcast map name to use.
         *
         * @param hostname the Hazelcast map name to use for feature state store
         */
        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        /**
         * Sets the Hazelcast configuration.
         *
         * @param jedisPoolConfig the Jedis Pool configuration {@link JedisPoolConfig}
         */
        public Builder config(JedisPoolConfig jedisPoolConfig) {
            this.jedisPoolConfig = jedisPoolConfig;
            return this;
        }

        /**
         * Creates a new {@link RedisStateRepository} using the current
         * settings.
         */
        public RedisStateRepository build() {
            return new RedisStateRepository(this);
        }

    }

}
