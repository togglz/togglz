package org.togglz.core.activation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;

import java.util.ArrayList;
import java.util.List;

class DefaultActivationStrategyProviderTest {

    private final DefaultActivationStrategyProvider provider = new DefaultActivationStrategyProvider();

    @Test
    void shouldLoadDefaultStrategies() {
        assertThat(provider.getActivationStrategies())
            .extracting("id")
            .contains(UsernameActivationStrategy.ID)
            .contains(GradualActivationStrategy.ID)
            .contains(ScriptEngineActivationStrategy.ID)
            .contains(ReleaseDateActivationStrategy.ID)
            .contains(ServerIpActivationStrategy.ID)
            .contains(UserRoleActivationStrategy.ID);

    }

    @Test
    void shouldNotContainCustomStrategyIfNotAdded() {
        assertThat(provider.getActivationStrategies())
            .extracting("id")
            .doesNotContain(CustomActivationStrategy.class.getSimpleName())
            .doesNotContain(AnotherCustomActivationStrategy.class.getSimpleName());

    }

    @Test
    void shouldContainCustomStrategyIfAddedBefore() {
        provider.addActivationStrategy(new CustomActivationStrategy());

        assertThat(provider.getActivationStrategies())
            .extracting("id")
            .contains(CustomActivationStrategy.class.getSimpleName());

    }

    @Test
    void shouldContainCustomStrategyIfAddedMultipleBefore() {
        List<ActivationStrategy> strategies = new ArrayList<>();
        strategies.add(new CustomActivationStrategy());
        strategies.add(new AnotherCustomActivationStrategy());

        provider.addActivationStrategies(strategies);

        assertThat(provider.getActivationStrategies())
                .extracting("id")
                .contains(CustomActivationStrategy.class.getSimpleName())
                .contains(AnotherCustomActivationStrategy.class.getSimpleName());

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
    }

    private static class AnotherCustomActivationStrategy implements ActivationStrategy {

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
    }
}
