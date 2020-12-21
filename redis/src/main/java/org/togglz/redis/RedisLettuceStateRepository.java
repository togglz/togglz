package org.togglz.redis;

import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisHashCommands;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import java.util.Map;
import java.util.Optional;

/**
 * A state repository which stores the feature state in Redis using Lettuce client.
 * <p>
 * The class provides a builder which can be used to configure the state repository instance
 * (e.g. StatefulConnection pool, key prefix).
 * </p>
 * <p>
 * A state repository is able to work in standalone mode (using StatefulRedisConnection) and
 * cluster mode (using StatefulRedisClusterConnection)
 * </p>
 * <p>
 * Lettuce doesn't provide default client creator (localhost as a default host, 6379 as a default port)
 * so an exception is made when StatefulConnection pool is not set correctly
 * </p>
 *
 * @author Jakub Klebek
 */
public class RedisLettuceStateRepository implements StateRepository {

    public static final String ENABLED_FIELD = "enabled";
    public static final String STRATEGY_FIELD = "strategy";
    public static final String PARAMETER_PREFIX = "parameter:";
    public static final int PARAMETER_PREFIX_LENGTH = PARAMETER_PREFIX.length();

    protected final GenericObjectPool<StatefulConnection<String, String>> pool;
    protected final String keyPrefix;

    private RedisLettuceStateRepository(final Builder builder) {
        keyPrefix = builder.keyPrefix;
        pool = Optional.ofNullable(builder.lettucePool)
            .orElseThrow(() -> new RedisLettuceStateRepositoryException("Missing lettuce pool configuration"));
    }

    @Override
    public FeatureState getFeatureState(final Feature feature) {
        try (final StatefulConnection<String, String> connection = pool.borrowObject()) {
            final RedisHashCommands<String, String> commands = getCommands(connection);
            final Map<String, String> redisMap = commands.hgetall(keyPrefix + feature.name());
            if (redisMap.isEmpty()) {
                return null;
            }
            final FeatureState featureState = new FeatureState(feature);
            featureState.setEnabled(Boolean.valueOf(redisMap.get(ENABLED_FIELD)));
            featureState.setStrategyId(redisMap.get(STRATEGY_FIELD));
            for (final Map.Entry<String, String> entry : redisMap.entrySet()) {
                final String key = entry.getKey();
                if (key.startsWith(PARAMETER_PREFIX)) {
                    featureState.setParameter(key.substring(PARAMETER_PREFIX_LENGTH), entry.getValue());
                }
            }
            return featureState;
        } catch (Exception e) {
            throw new RedisLettuceStateRepositoryException("Error while getting feature state", e);
        }
    }

    @Override
    public void setFeatureState(final FeatureState featureState) {
        try (final StatefulConnection<String, String> connection = pool.borrowObject()) {
            final RedisHashCommands<String, String> commands = getCommands(connection);
            final String featureKey = keyPrefix + featureState.getFeature().name();
            commands.hset(featureKey, ENABLED_FIELD, Boolean.toString(featureState.isEnabled()));
            final String strategyId = featureState.getStrategyId();
            if (strategyId != null) {
                commands.hset(featureKey, STRATEGY_FIELD, strategyId);
            }
            final Map<String, String> parameterMap = featureState.getParameterMap();
            if (parameterMap != null) {
                for (final Map.Entry<String, String> entry : parameterMap.entrySet()) {
                    commands.hset(featureKey, PARAMETER_PREFIX + entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            throw new RedisLettuceStateRepositoryException("Error while setting feature state", e);
        }
    }

    private RedisHashCommands<String, String> getCommands(final StatefulConnection<String, String> connection) {
        if (connection instanceof StatefulRedisConnection) {
            return ((StatefulRedisConnection) connection).sync();
        }

        return ((StatefulRedisClusterConnection) connection).sync();
    }

    /**
     * Builder for a {@link RedisLettuceStateRepository}.
     * <p>
     * Can be used as follows:
     * </p>
     * <pre>
     *      StateRepository stateRepository =
     *         new RedisLettuceStateRepository.Builder().
     *         lettucePool(ConnectionPoolSupport.createGenericObjectPool(
     *              () -> RedisClient.create(RedisURI.create("localhost", 6379)).connect(),
     *              new GenericObjectPoolConfig()))
     *         keyPrefix("toggles:").
     *         build();
     * </pre>
     */
    public static class Builder {

        private GenericObjectPool<StatefulConnection<String, String>> lettucePool;
        private String keyPrefix = "togglz:";

        /**
         * Sets the Lettuce Pool.
         *
         * @param lettucePool the Lettuce Pool {@link GenericObjectPool}
         */
        public Builder lettucePool(final GenericObjectPool<StatefulConnection<String, String>> lettucePool) {
            this.lettucePool = lettucePool;
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
         * Creates a new {@link RedisLettuceStateRepository} using the current settings.
         */
        public RedisLettuceStateRepository build() {
            return new RedisLettuceStateRepository(this);
        }

    }

    private static class RedisLettuceStateRepositoryException extends RuntimeException {

        public RedisLettuceStateRepositoryException(String message, Throwable cause) {
            super(message, cause);
        }

        public RedisLettuceStateRepositoryException(String message) {
            super(message);
        }
    }

}
