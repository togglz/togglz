package org.togglz.core.spi;

import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps context objects to {@link ContextAwareActivationStrategy} classes.
 *
 * @author Philip Sanetra
 */
public class ActivationStrategyContexts {

    /**
     * An {@link ActivationStrategyContexts} instance without any mappings
     */
    public static final ActivationStrategyContexts EMPTY = new ActivationStrategyContexts(new HashMap<>());

    private final Map<Class<? extends ContextAwareActivationStrategy>, Object> contextMap;

    public ActivationStrategyContexts(Map<Class<? extends ContextAwareActivationStrategy>, Object> contextMap) {
        this.contextMap = new HashMap<>(contextMap);
    }

    /**
     * Returns true if this instance has a context for the specified strategy.
     *
     * @return A context for the specified {@link ContextAwareActivationStrategy} class
     */
    public boolean hasContext(Class<? extends ContextAwareActivationStrategy> strategyClass) {
        return contextMap.containsKey(strategyClass);
    }

    /**
     * Returns the context to which the specified strategy is mapped, or null if there is no mapping.
     *
     * @return A context for the specified {@link ContextAwareActivationStrategy} class
     */
    public Object get(Class<? extends ContextAwareActivationStrategy> strategyClass) {
        return contextMap.get(strategyClass);
    }

    /**
     * Creates a {@link ActivationStrategyContexts} builder.
     *
     * @return a new {@link ActivationStrategyContexts} builder
     */
    public static ActivationStrategyContexts.Builder builder() {
        return new ActivationStrategyContexts.Builder();
    }

    public static class Builder {

        private final Map<Class<? extends ContextAwareActivationStrategy>, Object> contextMap = new HashMap<>();

        /**
         * Adds a context object for a specific {@link ContextAwareActivationStrategy} class.
         *
         * @param strategyClass A {@link ContextAwareActivationStrategy} class which instances should receive
         *                      the specified context as a parameter to
         *                      {@link ContextAwareActivationStrategy#isActive(FeatureState, FeatureUser, Object)} calls.
         * @param context       will be passed as a parameter to
         *                      {@link ContextAwareActivationStrategy#isActive(FeatureState, FeatureUser, Object)} calls.
         * @return An {@link ActivationStrategyContexts} instance to which the context was added
         */
        public ActivationStrategyContexts.Builder add(Class<? extends ContextAwareActivationStrategy> strategyClass, Object context) {
            contextMap.put(strategyClass, context);
            return this;
        }

        public ActivationStrategyContexts build() {
            return new ActivationStrategyContexts(contextMap);
        }

    }

}
