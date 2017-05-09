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
 * A state repository which stores the feature state in Redis.
 * <p>
 * The class provides a builder which can be used to configure the state repository instance
 * (e.g. Redis hostname, Jedis configuration, serialization).
 * </p>
 *
 * @author Cosmin Rentea
 */
public class RedisStateRepository implements StateRepository {

    public static final String PREFIX = "togglz-";
    public static final String ENABLED_FIELD = "enabled";
    public static final String STRATEGY_FIELD = "strategy";
    public static final String PARAMETERS_FIELD = "parameters";

    protected final JedisPool jedisPool;
    protected final JedisPoolConfig jedisPoolConfig;
    protected final String hostname;
    protected final MapSerializer mapSerializer;

    public RedisStateRepository() {
        jedisPoolConfig = null;
        hostname = Protocol.DEFAULT_HOST;
        mapSerializer = DefaultMapSerializer.multiline();
        jedisPool = createJedisPool();
    }

    private RedisStateRepository(final Builder builder) {
        jedisPoolConfig = builder.jedisPoolConfig;
        hostname = builder.hostname;
        mapSerializer = builder.mapSerializer;
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
            //TODO check if this if is necessary
            if (map == null) {
                return null;
            }

            final FeatureState featureState = new FeatureState(feature);
            featureState.setEnabled(Boolean.valueOf(map.get(ENABLED_FIELD)));
            featureState.setStrategyId(map.get(STRATEGY_FIELD));
            final String strategyParameters = map.get(PARAMETERS_FIELD);
            if (strategyParameters != null) {
                for (final Map.Entry<String, String> entry : mapSerializer.deserialize(strategyParameters).entrySet()) {
                    featureState.setParameter(entry.getKey(), entry.getValue());
                }
            }
            return featureState;
        }
        //TODO return null
    }

    @Override
    public void setFeatureState(final FeatureState featureState) {
        try (Jedis jedis = jedisPool.getResource()) {
            final String featureKey = PREFIX + featureState.getFeature().name();
            jedis.hset(featureKey, ENABLED_FIELD, Boolean.toString(featureState.isEnabled()));
            final String strategyId = featureState.getStrategyId();
            if (strategyId != null) {
                jedis.hset(featureKey, STRATEGY_FIELD, strategyId);
            }
            final Map<String, String> parameterMap = featureState.getParameterMap();
            if (parameterMap != null) {
                jedis.hset(featureKey, PARAMETERS_FIELD, mapSerializer.serialize(parameterMap));
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
     *         hostname("host").
     *         mapSerializer(DefaultMapSerializer.singleline()).
     *         build();
     * </pre>
     */
    public static class Builder {

        private String hostname = Protocol.DEFAULT_HOST;
        private JedisPoolConfig jedisPoolConfig = null;
        private MapSerializer mapSerializer = DefaultMapSerializer.multiline();

        /**
         * Creates a new builder for a {@link RedisStateRepository}.
         */
        public Builder() {
        }

        /**
         * Sets the Redis hostname to use.
         *
         * @param hostname the Redis hostname to use for storing feature states
         */
        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        /**
         * Sets the Jedis Pool configuration.
         *
         * @param jedisPoolConfig the Jedis Pool configuration {@link JedisPoolConfig}
         */
        public Builder config(JedisPoolConfig jedisPoolConfig) {
            this.jedisPoolConfig = jedisPoolConfig;
            return this;
        }

        /**
         * Sets the map serializer to be used when encoding/decoding parameters of the feature strategy.
         *
         * @param mapSerializer the map serializer {@link MapSerializer}
         */
        public Builder mapSerializer(MapSerializer mapSerializer) {
            this.mapSerializer = mapSerializer;
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
