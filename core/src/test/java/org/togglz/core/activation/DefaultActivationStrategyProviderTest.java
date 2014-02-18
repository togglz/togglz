package org.togglz.core.activation;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.togglz.core.spi.ActivationStrategy;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class DefaultActivationStrategyProviderTest {

    private DefaultActivationStrategyProvider activateStrategyProvider;

    @Before
    public void setup() {
        this.activateStrategyProvider = new DefaultActivationStrategyProvider();
    }

    @Test
    public void testGetActivationStrategy() {
        final List<ActivationStrategy> activationStrategys = this.activateStrategyProvider.getActivationStrategys();
        // extract IDs from list of resolved strategies for assertion
        final List<String> strategyIds = Lists.transform(activationStrategys, new Function<ActivationStrategy, String>() {
            @Override
            public String apply(ActivationStrategy strategy) {
                return strategy.getId();
            }
        });
        assertThat(strategyIds).contains(
            UsernameActivationStrategy.ID,
            GradualActivationStrategy.ID,
            ScriptEngineActivationStrategy.ID,
            ReleaseDateActivationStrategy.ID,
            ServerIpActivationStrategy.ID,
            UserRoleActivationStrategy.ID);
    }

}
