package org.togglz.core.manager;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.annotation.InfoLink;
import org.togglz.core.annotation.Label;
import org.togglz.core.annotation.Owner;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.spi.FeatureProvider;

public class EnumBasedFeatureProviderTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailForNull() {
        new EnumBasedFeatureProvider(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailForArrayWithNull() {
        new EnumBasedFeatureProvider(ValidFeatureEnum.class, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailForNonEnumType() {
        new EnumBasedFeatureProvider(NotAnEnum.class);
    }
    
    @Test(expected = IllegalStateException.class)
    public void shouldFailForDuplicateFeatureName() {
        
        EnumBasedFeatureProvider provider = new EnumBasedFeatureProvider();
        provider.addFeatureEnum(ValidFeatureEnum.class);
        provider.addFeatureEnum(DuplicateNameFeatureEnum.class); // should throw IllegalStateException
    }

    @Test
    public void shouldReturnCorrectListOfFeaturesForEnum() {

        FeatureProvider provider = new EnumBasedFeatureProvider(ValidFeatureEnum.class);
        assertThat(provider.getFeatures())
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

    @Test
    public void shouldReturnOwnerNameIfAnnotationPresent() {
        FeatureProvider provider = new EnumBasedFeatureProvider(ValidFeatureEnum.class);
        FeatureMetaData metaData = provider.getMetaData(ValidFeatureEnum.WITH_OWNER);
        assertThat(metaData.getAttributes())
            .containsValue("Christian");
    }

    @Test
    public void shouldReturnNullForOwnerNameByDefault() {
        FeatureProvider provider = new EnumBasedFeatureProvider(ValidFeatureEnum.class);
        FeatureMetaData metaData = provider.getMetaData(ValidFeatureEnum.FEATURE1);
        assertThat(metaData.getAttributes())
            .doesNotContainValue("Christian");
    }

    @Test
    public void shouldReturnInfoLinkIfAnnotationPresent() {
        FeatureProvider provider = new EnumBasedFeatureProvider(ValidFeatureEnum.class);
        FeatureMetaData metaData = provider.getMetaData(ValidFeatureEnum.WITH_LINK);
        assertThat(metaData.getAttributes())
            .containsValue("https://github.com/togglz/togglz/pull/33");
    }

    @Test
    public void shouldReturnNullForInfoLinkByDefault() {
        FeatureProvider provider = new EnumBasedFeatureProvider(ValidFeatureEnum.class);
        FeatureMetaData metaData = provider.getMetaData(ValidFeatureEnum.FEATURE1);
        assertThat(metaData.getAttributes())
            .doesNotContainValue("https://github.com/togglz/togglz/pull/33");
    }

    @Test
    public void shouldReturnCombinedFeatureListForMultipleEnums() {

        FeatureProvider provider = new EnumBasedFeatureProvider()
            .addFeatureEnum(ValidFeatureEnum.class)
            .addFeatureEnum(OtherFeatureEnum.class);

        // all feature are in the list
        assertThat(provider.getFeatures())
            .hasSize(ValidFeatureEnum.values().length + OtherFeatureEnum.values().length)
            .contains(ValidFeatureEnum.FEATURE1)
            .contains(OtherFeatureEnum.ADDITIONAL_FEATURE);

    }

    @Test
    public void shouldBuildMetadataForMultipleEnums() {

        FeatureProvider provider = new EnumBasedFeatureProvider()
            .addFeatureEnum(ValidFeatureEnum.class)
            .addFeatureEnum(OtherFeatureEnum.class);

        assertThat(provider.getMetaData(ValidFeatureEnum.FEATURE1).getLabel())
            .isEqualTo("First feature");
        assertThat(provider.getMetaData(OtherFeatureEnum.ADDITIONAL_FEATURE).getLabel())
            .isEqualTo("Additional Feature");

    }

    @Test
        public void shouldReturnCombinedFeatureListForMultipleEnumsViaConstructor() {

            FeatureProvider provider = new EnumBasedFeatureProvider(ValidFeatureEnum.class, OtherFeatureEnum.class);

            // all feature are in the list
            assertThat(provider.getFeatures())
                .hasSize(ValidFeatureEnum.values().length + OtherFeatureEnum.values().length)
                .contains(ValidFeatureEnum.FEATURE1)
                .contains(OtherFeatureEnum.ADDITIONAL_FEATURE);

        }

        @Test
        public void shouldBuildMetadataForMultipleEnumsViaConstructor() {

            FeatureProvider provider = new EnumBasedFeatureProvider(ValidFeatureEnum.class, OtherFeatureEnum.class);

            assertThat(provider.getMetaData(ValidFeatureEnum.FEATURE1).getLabel())
                .isEqualTo("First feature");
            assertThat(provider.getMetaData(OtherFeatureEnum.ADDITIONAL_FEATURE).getLabel())
                .isEqualTo("Additional Feature");

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

        FEATURE2,

        @Owner("Christian")
        WITH_OWNER,

        @InfoLink("https://github.com/togglz/togglz/pull/33")
        WITH_LINK;

    }

    public static enum OtherFeatureEnum implements Feature {

        @Label("Additional Feature")
        ADDITIONAL_FEATURE;

    }

    public static enum DuplicateNameFeatureEnum implements Feature {

        @Label("Duplicate feature name")
        FEATURE1;

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
