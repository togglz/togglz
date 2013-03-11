package org.togglz.core.manager;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.spi.FeatureProvider;

public class EnumBasedFeatureProviderTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailForNull() {
        new EnumBasedFeatureProvider(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailForNonEnumType() {
        new EnumBasedFeatureProvider(NotAnEnum.class);
    }

    @Test
    public void shouldReturnCorrectListOfFeaturesForEnum() {

        FeatureProvider provider = new EnumBasedFeatureProvider(ValidFeatureEnum.class);
        assertThat(provider.getFeatures())
            .hasSize(2)
            .containsSequence(ValidFeatureEnum.FEATURE1, ValidFeatureEnum.FEATURE2);

    }

    private static class NotAnEnum implements Feature {

        @Override
        public String name() {
            return "something";
        }

        @Override
        public boolean isActive() {
            return false;
        }

    }

    private static enum ValidFeatureEnum implements Feature {

        FEATURE1,
        FEATURE2;

        @Override
        public boolean isActive() {
            return FeatureContext.getFeatureManager().isActive(this);
        }

    }
}
