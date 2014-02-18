package org.togglz.core.activation;

import java.util.List;
import java.util.ServiceLoader;

import org.togglz.core.spi.ActivationStrategy;

/**
 * Implementations of this interface are responsible for providing the activation strategies. The default implementation
 * {@link DefaultActivationStrategyProvider} uses the standard JDK {@link ServiceLoader} mechanism to discover strategies.
 * 
 * @author Jesse Kershaw
 * 
 */
public interface ActivationStrategyProvider {

    List<ActivationStrategy> getActivationStrategies();

}
