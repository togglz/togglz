package org.togglz.redis;

import java.util.Map;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.util.Pool;

/**
 * A state repository which stores the feature state in Redis.
 * <p>
 * The class provides a builder which can be used to configure the state repository instance
 * (e.g. Redis hostname, Redis/Jedis configuration, serialization).
 * </p>
 *
 * @author Cosmin Rentea
 */
public class RedisStateRepository implements StateRepository {

    public static final String ENABLED_FIELD = "enabled";
    public static final String STRATEGY_FIELD = "strategy";
    public static final String PARAMETER_PREFIX = "parameter:";
    public static final int PARAMETER_PREFIX_LENGTH = PARAMETER_PREFIX.length();

    protected final Pool<Jedis> jedisPool;
    protected final String keyPrefix;

    private RedisStateRepository(final Builder builder) {
        keyPrefix = builder.keyPrefix;
        jedisPool = builder.jedisPool != null ? builder.jedisPool : new JedisPool();
    }

    @Override
    public FeatureState getFeatureState(final Feature feature) {
        try (final Jedis jedis = jedisPool.getResource()) {
            final Map<String, String> redisMap = jedis.hgetAll(keyPrefix + feature.name());
            if (redisMap == null || redisMap.size() == 0) {
                return null;
            }
            final FeatureState featureState = new FeatureState(feature);
            featureState.setEnabled(Boolean.parseBoolean(redisMap.get(ENABLED_FIELD)));
            featureState.setStrategyId(redisMap.get(STRATEGY_FIELD));
            for (final Map.Entry<String, String> entry : redisMap.entrySet()) {
                final String key = entry.getKey();
                if (key.startsWith(PARAMETER_PREFIX)) {
                    featureState.setParameter(key.substring(PARAMETER_PREFIX_LENGTH), entry.getValue());
                }
            }
            return featureState;
        }
    }

    @Override
    public void setFeatureState(final FeatureState featureState) {
        try (final Jedis jedis = jedisPool.getResource()) {
            final String featureKey = keyPrefix + featureState.getFeature().name();
            jedis.hset(featureKey, ENABLED_FIELD, Boolean.toString(featureState.isEnabled()));
            final String strategyId = featureState.getStrategyId();
            if (strategyId != null) {
                jedis.hset(featureKey, STRATEGY_FIELD, strategyId);
            }
            final Map<String, String> parameterMap = featureState.getParameterMap();
            if (parameterMap != null) {
                for (final Map.Entry<String, String> entry : parameterMap.entrySet()) {
                    jedis.hset(featureKey, PARAMETER_PREFIX + entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * Builder for a {@link RedisStateRepository}.
     * <p>
     * Can be used as follows:
     * </p>
     * <pre>
     *      StateRepository stateRepository =
     *         new RedisStateRepository.Builder().
     *         jedisPool(new JedisPool("hostname")).
     *         keyPrefix("toggles:").
     *         build();
     * </pre>
     */
    public static class Builder {

        private Pool<Jedis> jedisPool = null;
        private String keyPrefix = "togglz:";

        /**
         * Creates a new builder for a {@link RedisStateRepository}.
         */
        public Builder() {
            // intentionally empty
        }

        /**
         * Sets the Jedis Pool.
         *
         * @param jedisPool the Jedis Pool {@link JedisPool}
         */
        public Builder jedisPool(final Pool<Jedis> jedisPool) {
            this.jedisPool = jedisPool;
            return this;
        }

        /**
         * Sets the Redis key prefix to be used when getting or setting the state of the features.
         *
         * @param keyPrefix the key prefix to be used in Redis
         */
        public Builder keyPrefix(final String keyPrefix) {
            this.keyPrefix = keyPrefix;
            return this;
        }

        /**
         * Creates a new {@link RedisStateRepository} using the current settings.
         */
        public RedisStateRepository build() {
            return new RedisStateRepository(this);
        }

    }

}
