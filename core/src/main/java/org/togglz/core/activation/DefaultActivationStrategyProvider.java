package org.togglz.core.activation;

import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.util.Lists;

public class DefaultActivationStrategyProvider implements
		ActivationStrategyProvider {

	private final List<ActivationStrategy> strategies;
	
	public DefaultActivationStrategyProvider() {
        this.strategies = Lists.asList(ServiceLoader.load(ActivationStrategy.class).iterator());
	}
	
	@Override
	public List<ActivationStrategy> getActivationStrategys() {
		return Collections.unmodifiableList(this.strategies);
	}

}
