package org.togglz.core.metadata.enums;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.metadata.FeatureGroup;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

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
        FEATURE
    }

    @Test
    public void constructorWillPopulateGroupsFromAnnotations() throws Exception {
        // act
        EnumFeatureMetaData metaData = new EnumFeatureMetaData(TestFeatures.FEATURE);

        // assert
        Set<FeatureGroup> groups = metaData.getGroups();

        assertThat(groups, notNullValue());
        assertThat(groups.size(), is(2));

        // verify field level group is there
        FeatureGroup group1 = Iterables.find(groups, createFeatureGroupLabelPredicate(FIELD_LEVEL_GROUP_LABEL));
        assertThat(group1.contains(TestFeatures.FEATURE), is(true));

        // verify class level group is there
        FeatureGroup group2 = Iterables.find(groups, createFeatureGroupLabelPredicate(CLASS_LEVEL_GROUP_LABEL));
        assertThat(group2.contains(TestFeatures.FEATURE), is(true));
    }

    private Predicate<FeatureGroup> createFeatureGroupLabelPredicate(final String label) {
        return new Predicate<FeatureGroup>() {
            @Override
            public boolean apply(FeatureGroup group) {
                return group.getLabel().equals(label);
            }
        };
    }
}
