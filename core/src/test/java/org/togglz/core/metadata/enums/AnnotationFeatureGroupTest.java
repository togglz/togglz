package org.togglz.core.metadata.enums;

import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.metadata.FeatureGroup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.*;

class AnnotationFeatureGroupTest {

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

    @org.togglz.core.annotation.FeatureGroup
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface ClassLevelUnlabeledGroup {
    }

    @ClassLevelGroup
    @ClassLevelUnlabeledGroup
    private enum TestFeatures implements Feature {

        @FieldLevelGroup
        FEATURE
    }

    @Test
    void buildWillReturnNullWhenFeatureGroupAnnotationIsNotPresent() {
        FeatureGroup result = AnnotationFeatureGroup.build(Label.class);

        assertNull(result);
    }

    @Test
    void buildWillReturnFeatureGroupWhenFeatureGroupAnnotationIsPresentForFieldLevelGroup() {
        FeatureGroup result = AnnotationFeatureGroup.build(FieldLevelGroup.class);

        assertNotNull(result);
        assertEquals(FIELD_LEVEL_GROUP_LABEL, result.getLabel());
        assertTrue(result.contains(TestFeatures.FEATURE));
    }

    @Test
    void buildWillReturnFeatureGroupWhenFeatureGroupAnnotationIsPresentForClassLevelGroup() {
        FeatureGroup result = AnnotationFeatureGroup.build(ClassLevelGroup.class);

        assertNotNull(result);
        assertEquals(CLASS_LEVEL_GROUP_LABEL, result.getLabel());
        assertTrue(result.contains(TestFeatures.FEATURE));
    }

    @Test
    void buildWillReturnFeatureGroupWhenFeatureGroupAnnotationIsMissingLabelAnnotation() {
        FeatureGroup result = AnnotationFeatureGroup.build(ClassLevelUnlabeledGroup.class);

        assertNotNull(result);
        assertEquals("ClassLevelUnlabeledGroup", result.getLabel());
        assertTrue(result.contains(TestFeatures.FEATURE));
    }
}
