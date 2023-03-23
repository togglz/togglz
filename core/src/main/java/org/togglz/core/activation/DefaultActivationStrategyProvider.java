package org.togglz.core.activation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import org.togglz.core.spi.ActivationStrategy;

/**
 * Implementation of {@link ActivationStrategyProvider} that loads the strategies using the JDK {@link ServiceLoader}.
 * 
 * @author Jesse Kershaw
 */
public class DefaultActivationStrategyProvider implements ActivationStrategyProvider {

    private final List<ActivationStrategy> strategies = new ArrayList<>();

    public DefaultActivationStrategyProvider() {
        for (ActivationStrategy activationStrategy : ServiceLoader.load(ActivationStrategy.class)) {
            strategies.add(activationStrategy);
        }
    }

    public void addActivationStrategy(ActivationStrategy strategy) {
        this.strategies.add(strategy);
    }

    public void addActivationStrategies(List<ActivationStrategy> strategies) {
        this.strategies.addAll(strategies);
    }

    @Override
    public List<ActivationStrategy> getActivationStrategies() {
        return Collections.unmodifiableList(this.strategies);
    }

}
