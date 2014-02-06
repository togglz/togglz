package org.togglz.core.metadata.enums;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.metadata.FeatureGroup;

public class AnnotationFeatureGroupTest {

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
        FEATURE
    }

    @Test
    public void buildWillReturnNullWhenFeatureGroupAnnotationIsNotPresent() throws Exception {
        FeatureGroup result = AnnotationFeatureGroup.build(Label.class);

        assertThat(result, nullValue());
    }

    @Test
    public void buildWillReturnFeatureGroupWhenFeatureGroupAnnotationIsPresentForFieldLevelGroup() throws Exception {
        FeatureGroup result = AnnotationFeatureGroup.build(FieldLevelGroup.class);

        assertThat(result, notNullValue());
        assertThat(result.getLabel(), is(FIELD_LEVEL_GROUP_LABEL));
        assertThat(result.contains(TestFeatures.FEATURE), is(true));
    }

    @Test
    public void buildWillReturnFeatureGroupWhenFeatureGroupAnnotationIsPresentForClassLevelGroup() throws Exception {
        FeatureGroup result = AnnotationFeatureGroup.build(ClassLevelGroup.class);

        assertThat(result, notNullValue());
        assertThat(result.getLabel(), is(CLASS_LEVEL_GROUP_LABEL));
        assertThat(result.contains(TestFeatures.FEATURE), is(true));
    }
}
