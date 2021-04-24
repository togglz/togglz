package org.togglz.core.metadata.enums;

import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.annotation.ActivationParameter;
import org.togglz.core.annotation.DefaultActivationStrategy;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;
import org.togglz.core.metadata.FeatureGroup;
import org.togglz.core.repository.FeatureState;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class EnumFeatureMetaDataTest {

    private static final String FIELD_LEVEL_GROUP_LABEL = "Field Level Group Label";
    private static final String CLASS_LEVEL_GROUP_LABEL = "Class Level Group Label";

    @org.togglz.core.annotation.FeatureGroup
    @Label(FIELD_LEVEL_GROUP_LABEL)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface FieldLevelGroup {
    }

    @org.togglz.core.annotation.FeatureGroup
    @Label(CLASS_LEVEL_GROUP_LABEL)
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface ClassLevelGroup {
    }

    @ClassLevelGroup
    private enum TestFeatures implements Feature {

        @FieldLevelGroup
        FEATURE,

        @org.togglz.core.annotation.FeatureGroup("hello")
        FEATURE_WITHOUT_MANUALLY_CREATED_ANNOTATION,

        @org.togglz.core.annotation.FeatureGroup
        FEATURE_WITHOUT_MANUALLY_CREATED_ANNOTATION2,

        @EnabledByDefault
        @DefaultActivationStrategy(
                id = "SomeActivationId",
                parameters = {
                        @ActivationParameter(name = "SomeParameterName", value = "someValue1,someValue2"),
                        @ActivationParameter(name = "SomeParameterName2", value = "someValue3,someValue4")
                }
        )
        FEATURE_WITH_DEFAULT_STATE;
    }

    @Test
    void constructorWillPopulateGroupsFromAnnotations() {
        // act
        EnumFeatureMetaData metaData = new EnumFeatureMetaData(TestFeatures.FEATURE);

        // assert
        Set<FeatureGroup> groups = metaData.getGroups();

        assertNotNull(groups);
        assertEquals(2, groups.size());

        // verify field level group is there
        List<FeatureGroup> group1 = groups.stream().filter(createFeatureGroupLabelPredicate(FIELD_LEVEL_GROUP_LABEL)).collect(Collectors.toList());
        assertTrue(group1.get(0).contains(TestFeatures.FEATURE));

        // verify class level group is there
        List<FeatureGroup> group2 = groups.stream().filter(createFeatureGroupLabelPredicate(CLASS_LEVEL_GROUP_LABEL)).collect(Collectors.toList());
        assertTrue(group2.get(0).contains(TestFeatures.FEATURE));
    }

    @Test
    void constructorWillPopulateDefaultActivationStrategyFromAnnotations() {
        // act
        EnumFeatureMetaData metaData = new EnumFeatureMetaData(TestFeatures.FEATURE_WITH_DEFAULT_STATE);

        FeatureState featureState = metaData.getDefaultFeatureState();

        assertNotNull(featureState);
        assertTrue(featureState.isEnabled());
        assertEquals("SomeActivationId", featureState.getStrategyId());
        assertEquals("someValue1,someValue2", featureState.getParameter("SomeParameterName"));
        assertEquals("someValue3,someValue4", featureState.getParameter("SomeParameterName2"));
    }

    @Test
    void shouldCreateFeatureGroupWhenGroupNameIsAddedAsAnnotationValue() {
        EnumFeatureMetaData metaData = new EnumFeatureMetaData(TestFeatures.FEATURE_WITHOUT_MANUALLY_CREATED_ANNOTATION);

        Set<FeatureGroup> groups = metaData.getGroups();

        assertNotNull(groups);
        assertEquals(2, groups.size());

        List<FeatureGroup> group = groups.stream().filter(createFeatureGroupLabelPredicate("hello")).collect(Collectors.toList());
        assertTrue(group.get(0).contains(TestFeatures.FEATURE_WITHOUT_MANUALLY_CREATED_ANNOTATION));
    }

    @Test
    void shouldCreateFeatureGroupWhenGroupNameIsAddedAsAnnotationValue2() {
        EnumFeatureMetaData metaData = new EnumFeatureMetaData(TestFeatures.FEATURE_WITHOUT_MANUALLY_CREATED_ANNOTATION2);

        Set<FeatureGroup> groups = metaData.getGroups();

        assertNotNull(groups);
        assertEquals(2, groups.size());

        List<FeatureGroup> group = groups.stream().filter(createFeatureGroupLabelPredicate("")).collect(Collectors.toList());
        assertTrue(group.get(0).contains(TestFeatures.FEATURE_WITHOUT_MANUALLY_CREATED_ANNOTATION2));
    }

    private Predicate<FeatureGroup> createFeatureGroupLabelPredicate(final String label) {
        return group -> group.getLabel().equals(label);
    }
}
