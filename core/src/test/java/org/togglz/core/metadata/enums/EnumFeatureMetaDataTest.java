package org.togglz.core.metadata.enums;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EnumFeatureMetaDataTest {

    public static final String FIELD_LEVEL_GROUP_LABEL = "Field Level Group Label";
    public static final String CLASS_LEVEL_GROUP_LABEL = "Class Level Group Label";

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
    public void constructorWillPopulateGroupsFromAnnotations() throws Exception {
        // act
        EnumFeatureMetaData metaData = new EnumFeatureMetaData(TestFeatures.FEATURE);

        // assert
        Set<FeatureGroup> groups = metaData.getGroups();

        assertNotNull(groups);
        assertEquals(2, groups.size());

        // verify field level group is there
        FeatureGroup group1 = Iterables.find(groups, createFeatureGroupLabelPredicate(FIELD_LEVEL_GROUP_LABEL));
        assertTrue(group1.contains(TestFeatures.FEATURE));

        // verify class level group is there
        FeatureGroup group2 = Iterables.find(groups, createFeatureGroupLabelPredicate(CLASS_LEVEL_GROUP_LABEL));
        assertTrue(group2.contains(TestFeatures.FEATURE));
    }

    @Test
    public void constructorWillPopulateDefaultActivationStrategyFromAnnotations() throws Exception {
        // act
        EnumFeatureMetaData metaData = new EnumFeatureMetaData(TestFeatures.FEATURE_WITH_DEFAULT_STATE);

        FeatureState featureState = metaData.getDefaultFeatureState();

        assertNotNull(featureState);
        assertTrue(featureState.isEnabled());
        assertEquals("SomeActivationId", featureState.getStrategyId());
        assertEquals("someValue1,someValue2", featureState.getParameter("SomeParameterName"));
        assertEquals("someValue3,someValue4", featureState.getParameter("SomeParameterName2"));
    }

    private Predicate<FeatureGroup> createFeatureGroupLabelPredicate(final String label) {
        return group -> group.getLabel().equals(label);
    }
}
