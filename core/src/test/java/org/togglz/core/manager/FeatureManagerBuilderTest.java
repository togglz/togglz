package org.togglz.core.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.activation.ActivationStrategyProvider;
import org.togglz.core.activation.DefaultActivationStrategyProvider;
import org.togglz.core.activation.Parameter;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;

public class FeatureManagerBuilderTest {

    @Test
    public void shouldAddStrategyIfUsingDefaultProvider() {

        DefaultActivationStrategyProvider provider = new DefaultActivationStrategyProvider();

        FeatureManagerBuilder.begin()
            .featureEnum(Features.class)
            .activationStrategyProvider(provider)
            .activationStrategy(new CustomActivationStrategy())
            .build();

        assertThat(provider.getActivationStrategies())
            .extracting("id")
            .contains(CustomActivationStrategy.class.getSimpleName());

    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfAddingStrategyWithCustomProvider() {

        CustomStrategyProvider provider = new CustomStrategyProvider();

        FeatureManagerBuilder.begin()
            .featureEnum(Features.class)
            .activationStrategyProvider(provider)
            .activationStrategy(new CustomActivationStrategy())
            .build();

    }

    private enum Features implements Feature {
        SOME_FEATURE
    }

    private static class CustomStrategyProvider implements ActivationStrategyProvider {

        @Override
        public List<ActivationStrategy> getActivationStrategies() {
            return Collections.emptyList();
        }

    }

    private static class CustomActivationStrategy implements ActivationStrategy {

        @Override
        public boolean isActive(FeatureState featureState, FeatureUser user) {
            return false;
        }

        @Override
        public Parameter[] getParameters() {
            return new Parameter[0];
        }

        @Override
        public String getName() {
            return getId();
        }

        @Override
        public String getId() {
            return this.getClass().getSimpleName();
        }
    };

}
