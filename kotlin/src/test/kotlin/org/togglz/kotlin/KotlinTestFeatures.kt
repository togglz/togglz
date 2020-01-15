package org.togglz.kotlin

import org.togglz.core.annotation.EnabledByDefault
import org.togglz.core.annotation.FeatureAttribute
import org.togglz.core.annotation.FeatureGroup
import org.togglz.core.annotation.Label
import org.togglz.core.context.FeatureContext
import java.lang.annotation.ElementType
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.RetentionPolicy.RUNTIME

const val FIELD_LEVEL_GROUP_LABEL = "Field Level Group Label"
const val CLASS_LEVEL_GROUP_LABEL = "Class Level Group Label"

@FeatureGroup
@Label(FIELD_LEVEL_GROUP_LABEL)
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
private annotation class FieldLevelGroup

@FeatureGroup
@Label(CLASS_LEVEL_GROUP_LABEL)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
private annotation class ClassLevelGroup

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
@FeatureAttribute("TestAttribute")
annotation class TestAttribute(val value: String)


@ClassLevelGroup
enum class KotlinTestFeatures {
    @EnabledByDefault
    @TestAttribute(value = "fooAttributeValue")
    FOO,

    @Label("bar feature")
    @FieldLevelGroup()
    BAR;

    fun isActive(): Boolean {
        return FeatureContext.getFeatureManager().isActive { name }
    }
}


