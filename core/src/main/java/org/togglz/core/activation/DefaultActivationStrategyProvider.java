package org.togglz.core.activation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.togglz.core.spi.ActivationStrategy;

/**
 * Implementation of {@link ActivationStrategyProvider} that loads the strategies using the JDK {@link ServiceLoader}.
 * 
 * @author Jesse Kershaw
 */
public class DefaultActivationStrategyProvider implements ActivationStrategyProvider {

    private final List<ActivationStrategy> strategies = new ArrayList<ActivationStrategy>();

    public DefaultActivationStrategyProvider() {
        Iterator<ActivationStrategy> iterator = ServiceLoader.load(ActivationStrategy.class).iterator();
        while (iterator.hasNext()) {
            strategies.add((ActivationStrategy) iterator.next());
        }
    }

    public void addActivationStrategy(ActivationStrategy strategy) {
        this.strategies.add(strategy);
    }

    @Override
    public List<ActivationStrategy> getActivationStrategies() {
        return Collections.unmodifiableList(this.strategies);
    }

}
