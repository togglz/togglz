package org.togglz.core.util;

import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.FeatureGroup;
import org.togglz.core.annotation.Label;

import java.lang.annotation.*;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

public class FeatureAnnotationsTest {

    @FeatureGroup
    @Label("Class Level Group Label")
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface ClassLevelGroup {
    }

    @ClassLevelGroup
    private enum MyFeature implements Feature {

        @Label("Some feature with a label")
        FEATURE_WITH_LABEL,

        // no label annotation
        FEATURE_WITHOUT_LABEL,

        @EnabledByDefault
        FEATURE_ENABLED_BY_DEFAULT
    }

    private enum MyFeature2 implements Feature {
        FEATURE_WITH_NO_ANNOTATIONS
    }

    @Test
    void testGetLabel() {
        assertEquals("Some feature with a label", FeatureAnnotations.getLabel(MyFeature.FEATURE_WITH_LABEL));
        assertEquals("FEATURE_WITHOUT_LABEL", FeatureAnnotations.getLabel(MyFeature.FEATURE_WITHOUT_LABEL));
    }

    @Test
    void testIsEnabledByDefault() {
        assertFalse(FeatureAnnotations.isEnabledByDefault(MyFeature.FEATURE_WITH_LABEL));
        assertFalse(FeatureAnnotations.isEnabledByDefault(MyFeature.FEATURE_WITHOUT_LABEL));
        assertTrue(FeatureAnnotations.isEnabledByDefault(MyFeature.FEATURE_ENABLED_BY_DEFAULT));
    }

    @Test
    void getAnnotationsWillReturnBothFieldAndClassLevelAnnotations() {
        Set<Annotation> result = FeatureAnnotations.getAnnotations(MyFeature.FEATURE_ENABLED_BY_DEFAULT);

        assertNotNull(result);
        assertEquals(2, result.size());

        // verify both EnabledByDefault and ClassLevelGroup are there
        List<Annotation> enabledByDefault = result.stream().filter(createAnnotationTypePredicate(EnabledByDefault.class)).collect(toList());
        assertFalse(enabledByDefault.isEmpty());

        List<Annotation> classLevelGroup = result.stream().filter(createAnnotationTypePredicate(ClassLevelGroup.class)).collect(toList());
        assertFalse(classLevelGroup.isEmpty());
    }

    @Test
    void getAnnotationsWillReturnEmptySetWhenThereAreNoAnnotations() {
        Set<Annotation> result = FeatureAnnotations.getAnnotations(MyFeature2.FEATURE_WITH_NO_ANNOTATIONS);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    private Predicate<Annotation> createAnnotationTypePredicate(final Class<? extends Annotation> annotationType) {
        return annotation -> annotation.annotationType().equals(annotationType);
    }

    @Test
    void getAnnotationWillReturnFieldLevelAnnotation() {
        EnabledByDefault result = FeatureAnnotations.getAnnotation(MyFeature.FEATURE_ENABLED_BY_DEFAULT, EnabledByDefault.class);
        assertNotNull(result);
    }

    @Test
    void getAnnotationWillReturnClassLevelAnnotation() {
        ClassLevelGroup result = FeatureAnnotations.getAnnotation(MyFeature.FEATURE_ENABLED_BY_DEFAULT, ClassLevelGroup.class);
        assertNotNull(result);
    }

    @Test
    void getAnnotationWillReturnNullWhenAnnotationDoesNotExist() {
        Label result = FeatureAnnotations.getAnnotation(MyFeature.FEATURE_ENABLED_BY_DEFAULT, Label.class);
        assertNull(result);
    }
}
