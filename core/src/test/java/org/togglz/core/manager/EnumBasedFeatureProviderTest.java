package org.togglz.core.manager;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.metadata.FeatureMetaData;
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

    @Test
    public void shouldReturnMetaDataWithCorrectLabel() {

        FeatureProvider provider = new EnumBasedFeatureProvider(ValidFeatureEnum.class);
        FeatureMetaData metaData = provider.getMetaData(ValidFeatureEnum.FEATURE1);
        assertThat(metaData.getLabel()).isEqualTo("First feature");

    }

    @Test
    public void shouldReturnMetaDataWhenRequestedWithOtherFeatureImplementation() {

        FeatureProvider provider = new EnumBasedFeatureProvider(ValidFeatureEnum.class);
        FeatureMetaData metaData =
            provider.getMetaData(new OtherFeatureImpl(ValidFeatureEnum.FEATURE1.name()));
        assertThat(metaData.getLabel()).isEqualTo("First feature");

    }

    private static class NotAnEnum implements Feature {

        @Override
        public String name() {
            return "something";
        }

    }

    private static enum ValidFeatureEnum implements Feature {

        @Label("First feature")
        FEATURE1,

        FEATURE2;

    }

    private class OtherFeatureImpl implements Feature {

        private final String name;

        public OtherFeatureImpl(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }

    }

}
