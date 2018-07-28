package org.togglz.core.manager;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.togglz.core.GenericEnumFeature;
import org.togglz.core.annotation.InfoLink;
import org.togglz.core.annotation.Label;
import org.togglz.core.annotation.Owner;
import org.togglz.core.metadata.FeatureMetaData;

public class GenericEnumBasedFeatureProviderTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailForNull() {
        new GenericEnumBasedFeatureProvider(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailForArrayWithNull() {
        new GenericEnumBasedFeatureProvider(ValidFeatureEnum.class, null);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailForDuplicateFeatureName() {

        GenericEnumBasedFeatureProvider provider = new GenericEnumBasedFeatureProvider();
        provider.addFeatureEnum(ValidFeatureEnum.class);
        provider.addFeatureEnum(DuplicateNameFeatureEnum.class); // should throw IllegalStateException
    }

    @Test
    public void shouldReturnCorrectListOfFeaturesForEnum() {

        GenericEnumBasedFeatureProvider provider = new GenericEnumBasedFeatureProvider(ValidFeatureEnum.class);
        assertThat(provider.getFeatures())
            .containsSequence(new GenericEnumFeature(ValidFeatureEnum.FEATURE1), new GenericEnumFeature(ValidFeatureEnum.FEATURE2));

    }

    @Test
    public void shouldReturnMetaDataWithCorrectLabel() {

        GenericEnumBasedFeatureProvider provider = new GenericEnumBasedFeatureProvider(ValidFeatureEnum.class);
        FeatureMetaData metaData = provider.getMetaData(ValidFeatureEnum.FEATURE1);
        assertThat(metaData.getLabel()).isEqualTo("First feature");

    }

    @Test
    public void shouldReturnOwnerNameIfAnnotationPresent() {
        GenericEnumBasedFeatureProvider provider = new GenericEnumBasedFeatureProvider(ValidFeatureEnum.class);
        FeatureMetaData metaData = provider.getMetaData(ValidFeatureEnum.WITH_OWNER);
        assertThat(metaData.getAttributes())
            .containsValue("Christian");
    }

    @Test
    public void shouldReturnNullForOwnerNameByDefault() {
        GenericEnumBasedFeatureProvider provider = new GenericEnumBasedFeatureProvider(ValidFeatureEnum.class);
        FeatureMetaData metaData = provider.getMetaData(ValidFeatureEnum.FEATURE1);
        assertThat(metaData.getAttributes())
            .doesNotContainValue("Christian");
    }

    @Test
    public void shouldReturnInfoLinkIfAnnotationPresent() {
        GenericEnumBasedFeatureProvider provider = new GenericEnumBasedFeatureProvider(ValidFeatureEnum.class);
        FeatureMetaData metaData = provider.getMetaData(ValidFeatureEnum.WITH_LINK);
        assertThat(metaData.getAttributes())
            .containsValue("https://github.com/togglz/togglz/pull/33");
    }

    @Test
    public void shouldReturnNullForInfoLinkByDefault() {
        GenericEnumBasedFeatureProvider provider = new GenericEnumBasedFeatureProvider(ValidFeatureEnum.class);
        FeatureMetaData metaData = provider.getMetaData(ValidFeatureEnum.FEATURE1);
        assertThat(metaData.getAttributes())
            .doesNotContainValue("https://github.com/togglz/togglz/pull/33");
    }

    @Test
    public void shouldReturnCombinedFeatureListForMultipleEnums() {

        GenericEnumBasedFeatureProvider provider = new GenericEnumBasedFeatureProvider()
            .addFeatureEnum(ValidFeatureEnum.class)
            .addFeatureEnum(OtherFeatureEnum.class);

        // all feature are in the list
        assertThat(provider.getFeatures())
            .hasSize(ValidFeatureEnum.values().length + OtherFeatureEnum.values().length)
            .contains(new GenericEnumFeature(ValidFeatureEnum.FEATURE1))
            .contains(new GenericEnumFeature(OtherFeatureEnum.ADDITIONAL_FEATURE));

    }

    @Test
    public void shouldBuildMetadataForMultipleEnums() {

        GenericEnumBasedFeatureProvider provider = new GenericEnumBasedFeatureProvider()
            .addFeatureEnum(ValidFeatureEnum.class)
            .addFeatureEnum(OtherFeatureEnum.class);

        assertThat(provider.getMetaData(ValidFeatureEnum.FEATURE1).getLabel())
            .isEqualTo("First feature");
        assertThat(provider.getMetaData(OtherFeatureEnum.ADDITIONAL_FEATURE).getLabel())
            .isEqualTo("Additional Feature");

    }

    @Test
    public void shouldReturnCombinedFeatureListForMultipleEnumsViaConstructor() {

        GenericEnumBasedFeatureProvider provider = new GenericEnumBasedFeatureProvider(ValidFeatureEnum.class, OtherFeatureEnum.class);

        // all feature are in the list
        assertThat(provider.getFeatures())
            .hasSize(ValidFeatureEnum.values().length + OtherFeatureEnum.values().length)
            .contains(new GenericEnumFeature(ValidFeatureEnum.FEATURE1))
            .contains(new GenericEnumFeature(OtherFeatureEnum.ADDITIONAL_FEATURE));

    }

    @Test
    public void shouldBuildMetadataForMultipleEnumsViaConstructor() {

        GenericEnumBasedFeatureProvider provider = new GenericEnumBasedFeatureProvider(ValidFeatureEnum.class, OtherFeatureEnum.class);

        assertThat(provider.getMetaData(ValidFeatureEnum.FEATURE1).getLabel())
            .isEqualTo("First feature");
        assertThat(provider.getMetaData(OtherFeatureEnum.ADDITIONAL_FEATURE).getLabel())
            .isEqualTo("Additional Feature");

    }

    @Test
    public void shouldNotAllowTheDefaultFeatureStateToBeChangedByExternalClasses() {
        GenericEnumBasedFeatureProvider provider = new GenericEnumBasedFeatureProvider(ValidFeatureEnum.class, OtherFeatureEnum.class);

        FeatureMetaData metaData = provider.getMetaData(ValidFeatureEnum.FEATURE1);
        assertThat(metaData.getDefaultFeatureState().isEnabled()).isEqualTo(false);
        metaData.getDefaultFeatureState().setEnabled(true);

        assertThat(provider.getMetaData(ValidFeatureEnum.FEATURE1).getDefaultFeatureState().isEnabled()).isEqualTo(false);
    }

    private static enum ValidFeatureEnum {

        @Label("First feature")
        FEATURE1,

        FEATURE2,

        @Owner("Christian")
        WITH_OWNER,

        @InfoLink("https://github.com/togglz/togglz/pull/33")
        WITH_LINK;

    }

    public static enum OtherFeatureEnum {

        @Label("Additional Feature")
        ADDITIONAL_FEATURE;

    }

    public static enum DuplicateNameFeatureEnum {

        @Label("Duplicate feature name")
        FEATURE1;

    }
}
