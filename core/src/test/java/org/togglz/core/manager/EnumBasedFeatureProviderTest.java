package org.togglz.core.manager;

import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.annotation.InfoLink;
import org.togglz.core.annotation.Label;
import org.togglz.core.annotation.Owner;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.spi.FeatureProvider;

import static org.junit.jupiter.api.Assertions.*;

public class EnumBasedFeatureProviderTest {

    @Test
    void shouldFailForNull() {
        assertThrows(IllegalArgumentException.class, () -> new EnumBasedFeatureProvider(null));
    }

    @Test
    void shouldFailForArrayWithNull() {
        assertThrows(IllegalArgumentException.class, () -> new EnumBasedFeatureProvider(ValidFeatureEnum.class, null));
    }

    @Test
    void shouldFailForNonEnumType() {
        assertThrows(IllegalArgumentException.class, () -> new EnumBasedFeatureProvider(NotAnEnum.class));
    }

    @Test
    void shouldFailForDuplicateFeatureName() {
        EnumBasedFeatureProvider provider = new EnumBasedFeatureProvider();
        provider.addFeatureEnum(ValidFeatureEnum.class);
        assertThrows(IllegalStateException.class, () -> provider.addFeatureEnum(DuplicateNameFeatureEnum.class));
    }

    @Test
    void shouldReturnCorrectListOfFeaturesForEnum() {
        FeatureProvider provider = new EnumBasedFeatureProvider(ValidFeatureEnum.class);
        assertEquals(provider.getFeatures().toArray()[0], ValidFeatureEnum.FEATURE1);
        assertEquals(provider.getFeatures().toArray()[1], ValidFeatureEnum.FEATURE2);
    }

    @Test
    void shouldReturnMetaDataWithCorrectLabel() {
        FeatureProvider provider = new EnumBasedFeatureProvider(ValidFeatureEnum.class);
        FeatureMetaData metaData = provider.getMetaData(ValidFeatureEnum.FEATURE1);

        assertEquals("First feature", metaData.getLabel());
    }

    @Test
    void shouldReturnMetaDataWhenRequestedWithOtherFeatureImplementation() {

        FeatureProvider provider = new EnumBasedFeatureProvider(ValidFeatureEnum.class);
        FeatureMetaData metaData =
                provider.getMetaData(new OtherFeatureImpl(ValidFeatureEnum.FEATURE1.name()));
        assertEquals("First feature", metaData.getLabel());
    }

    @Test
    void shouldReturnOwnerNameIfAnnotationPresent() {
        FeatureProvider provider = new EnumBasedFeatureProvider(ValidFeatureEnum.class);
        FeatureMetaData metaData = provider.getMetaData(ValidFeatureEnum.WITH_OWNER);

        assertTrue(metaData.getAttributes().containsValue("Christian"));
    }

    @Test
    void shouldReturnNullForOwnerNameByDefault() {
        FeatureProvider provider = new EnumBasedFeatureProvider(ValidFeatureEnum.class);
        FeatureMetaData metaData = provider.getMetaData(ValidFeatureEnum.FEATURE1);

        assertFalse(metaData.getAttributes().containsValue("Christian"));
    }

    @Test
    void shouldReturnInfoLinkIfAnnotationPresent() {
        FeatureProvider provider = new EnumBasedFeatureProvider(ValidFeatureEnum.class);
        FeatureMetaData metaData = provider.getMetaData(ValidFeatureEnum.WITH_LINK);

        assertTrue(metaData.getAttributes().containsValue("https://github.com/togglz/togglz/pull/33"));
    }

    @Test
    void shouldReturnNullForInfoLinkByDefault() {
        FeatureProvider provider = new EnumBasedFeatureProvider(ValidFeatureEnum.class);
        FeatureMetaData metaData = provider.getMetaData(ValidFeatureEnum.FEATURE1);

        assertFalse(metaData.getAttributes().containsValue("https://github.com/togglz/togglz/pull/33"));
    }

    @Test
    void shouldReturnCombinedFeatureListForMultipleEnums() {

        FeatureProvider provider = new EnumBasedFeatureProvider()
                .addFeatureEnum(ValidFeatureEnum.class)
                .addFeatureEnum(OtherFeatureEnum.class);

        // all feature are in the list
        assertEquals(ValidFeatureEnum.values().length + OtherFeatureEnum.values().length, provider.getFeatures().size());
        assertTrue(provider.getFeatures().contains(ValidFeatureEnum.FEATURE1));
        assertTrue(provider.getFeatures().contains(OtherFeatureEnum.ADDITIONAL_FEATURE));
    }

    @Test
    void shouldBuildMetadataForMultipleEnums() {

        FeatureProvider provider = new EnumBasedFeatureProvider()
                .addFeatureEnum(ValidFeatureEnum.class)
                .addFeatureEnum(OtherFeatureEnum.class);

        assertEquals("First feature", provider.getMetaData(ValidFeatureEnum.FEATURE1).getLabel());
        assertEquals("Additional Feature", provider.getMetaData(OtherFeatureEnum.ADDITIONAL_FEATURE).getLabel());
    }

    @Test
    void shouldReturnCombinedFeatureListForMultipleEnumsViaConstructor() {

        FeatureProvider provider = new EnumBasedFeatureProvider(ValidFeatureEnum.class, OtherFeatureEnum.class);

        // all feature are in the list
        assertEquals(ValidFeatureEnum.values().length + OtherFeatureEnum.values().length, provider.getFeatures().size());
        assertTrue(provider.getFeatures().contains(ValidFeatureEnum.FEATURE1));
        assertTrue(provider.getFeatures().contains(OtherFeatureEnum.ADDITIONAL_FEATURE));
    }

    @Test
    void shouldBuildMetadataForMultipleEnumsViaConstructor() {
        FeatureProvider provider = new EnumBasedFeatureProvider(ValidFeatureEnum.class, OtherFeatureEnum.class);

        assertEquals("First feature", provider.getMetaData(ValidFeatureEnum.FEATURE1).getLabel());
        assertEquals("Additional Feature", provider.getMetaData(OtherFeatureEnum.ADDITIONAL_FEATURE).getLabel());
    }

    @Test
    void shouldNotAllowTheDefaultFeatureStateToBeChangedByExternalClasses() {
        FeatureProvider provider = new EnumBasedFeatureProvider(ValidFeatureEnum.class, OtherFeatureEnum.class);

        FeatureMetaData metaData = provider.getMetaData(ValidFeatureEnum.FEATURE1);
        assertFalse(metaData.getDefaultFeatureState().isEnabled());
        metaData.getDefaultFeatureState().setEnabled(true);

        assertFalse(provider.getMetaData(ValidFeatureEnum.FEATURE1).getDefaultFeatureState().isEnabled());
    }

    @Test
    void providerShouldNotEagerlyCreateFeaturesSet() {
        FeatureProvider provider = new EnumBasedFeatureProvider();
        assertEquals(0, provider.getFeatures().size());
    }

    @Test
    void providerShouldThrowIllegalStateExceptionIfGetMetaDataIsCalledonEmptyProviderInstance() {
        FeatureProvider provider = new EnumBasedFeatureProvider();
        assertThrows(IllegalStateException.class, () -> {
            provider.getMetaData(ValidFeatureEnum.FEATURE1).getDefaultFeatureState().isEnabled();
        });
    }
    private static class NotAnEnum implements Feature {

        @Override
        public String name() {
            return "something";
        }

    }

    private enum ValidFeatureEnum implements Feature {

        @Label("First feature")
        FEATURE1,

        FEATURE2,

        @Owner("Christian")
        WITH_OWNER,

        @InfoLink("https://github.com/togglz/togglz/pull/33")
        WITH_LINK

    }

    public enum OtherFeatureEnum implements Feature {

        @Label("Additional Feature")
        ADDITIONAL_FEATURE

    }

    public enum DuplicateNameFeatureEnum implements Feature {
        @Label("Duplicate feature name")
        FEATURE1
    }

    private static class OtherFeatureImpl implements Feature {

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
