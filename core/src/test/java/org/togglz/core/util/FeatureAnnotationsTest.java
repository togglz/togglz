package org.togglz.core.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static junit.framework.Assert.assertEquals;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.FeatureGroup;
import org.togglz.core.annotation.Label;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class FeatureAnnotationsTest {

    @FeatureGroup
    @Label("Class Level Group Label")
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface ClassLevelGroup {
    }

    @ClassLevelGroup
    private static enum MyFeature implements Feature {

        @Label("Some feature with a label")
        FEATURE_WITH_LABEL,

        // no label annotation
        FEATURE_WITHOUT_LABEL,

        @EnabledByDefault
        FEATURE_ENABLED_BY_DEFAULT;

    }

    private static enum MyFeature2 implements Feature {

        FEATURE_WITH_NO_ANNOTATIONS

    }

    @Test
    public void testGetLabel() {

        assertEquals("Some feature with a label", FeatureAnnotations.getLabel(MyFeature.FEATURE_WITH_LABEL));
        assertEquals("FEATURE_WITHOUT_LABEL", FeatureAnnotations.getLabel(MyFeature.FEATURE_WITHOUT_LABEL));

    }

    @Test
    public void testIsEnabledByDefault() {

        assertEquals(false, FeatureAnnotations.isEnabledByDefault(MyFeature.FEATURE_WITH_LABEL));
        assertEquals(false, FeatureAnnotations.isEnabledByDefault(MyFeature.FEATURE_WITHOUT_LABEL));
        assertEquals(true, FeatureAnnotations.isEnabledByDefault(MyFeature.FEATURE_ENABLED_BY_DEFAULT));

    }

    @Test
    public void getAnnotationsWillReturnBothFieldAndClassLevelAnnotations() throws Exception {
        Set<Annotation> result = FeatureAnnotations.getAnnotations(MyFeature.FEATURE_ENABLED_BY_DEFAULT);

        assertThat(result, notNullValue());
        assertThat(result.size(), is(2));

        // verify both EnabledByDefault and ClassLevelGroup are there
        Iterables.find(result, createAnnotationTypePredicate(EnabledByDefault.class));
        Iterables.find(result, createAnnotationTypePredicate(ClassLevelGroup.class));
    }

    @Test
    public void getAnnotationsWillReturnEmptySetWhenThereAreNoAnnotations() throws Exception {
        Set<Annotation> result = FeatureAnnotations.getAnnotations(MyFeature2.FEATURE_WITH_NO_ANNOTATIONS);

        assertThat(result, notNullValue());
        assertThat(result.size(), is(0));
    }

    private Predicate<Annotation> createAnnotationTypePredicate(final Class<? extends Annotation> annotationType) {
        return new Predicate<Annotation>() {
            @Override
            public boolean apply(Annotation annotation) {
                return annotation.annotationType().equals(annotationType);
            }
        };
    }

    @Test
    public void getAnnotationWillReturnFieldLevelAnnotation() throws Exception {
        EnabledByDefault result = FeatureAnnotations.getAnnotation(MyFeature.FEATURE_ENABLED_BY_DEFAULT, EnabledByDefault.class);
        assertThat(result, notNullValue());
    }

    @Test
    public void getAnnotationWillReturnClassLevelAnnotation() throws Exception {
        ClassLevelGroup result = FeatureAnnotations.getAnnotation(MyFeature.FEATURE_ENABLED_BY_DEFAULT, ClassLevelGroup.class);
        assertThat(result, notNullValue());
    }

    @Test
    public void getAnnotationWillReturnNullWhenAnnotationDoesNotExist() throws Exception {
        Label result = FeatureAnnotations.getAnnotation(MyFeature.FEATURE_ENABLED_BY_DEFAULT, Label.class);
        assertThat(result, nullValue());
    }
}
