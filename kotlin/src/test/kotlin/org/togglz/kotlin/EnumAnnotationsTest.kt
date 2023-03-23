package org.togglz.kotlin

import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.togglz.core.annotation.Label

class EnumAnnotationsTest {

    @Test
    fun `should get label from enum`() {
        val label = EnumAnnotations.getLabel(KotlinTestFeatures.BAR)

        label shouldBe "bar feature"
    }

    @Test
    fun `should be enabled by default`() {
        val notEnabledByDefault = EnumAnnotations.isEnabledByDefault(KotlinTestFeatures.BAR)

        notEnabledByDefault shouldBe false

        val enabledByDefault = EnumAnnotations.isEnabledByDefault(KotlinTestFeatures.FOO)

        enabledByDefault shouldBe true
    }

    @Test
    fun getAnnotation() {
        val annotation = EnumAnnotations.getAnnotation(KotlinTestFeatures.BAR, Label::class.java)

        annotation!!.value shouldBe "bar feature"
    }

    @Test
    fun getAnnotations() {
        val annotations = EnumAnnotations.getAnnotations(KotlinTestFeatures.BAR)

        annotations.size shouldBe 3
        annotations.map { it.annotationClass.simpleName } shouldContainAll listOf("ClassLevelGroup", "Label", "FieldLevelGroup")
    }
}