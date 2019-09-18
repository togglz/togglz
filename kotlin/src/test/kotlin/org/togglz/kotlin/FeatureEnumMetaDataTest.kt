package org.togglz.kotlin

import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

internal class FeatureEnumMetaDataTest {

    @Test
    internal fun `should get label`() {
        val label = FeatureEnumMetaData(KotlinTestFeatures.BAR, FeatureEnum(KotlinTestFeatures.BAR)).label

        label shouldBe "bar feature"
    }

    @Test
    internal fun `should get defaultFeatureState`() {
        val defaultFeatureState = FeatureEnumMetaData(KotlinTestFeatures.BAR, FeatureEnum(KotlinTestFeatures.BAR)).defaultFeatureState

        defaultFeatureState.isEnabled shouldBe false

        val enabledFeatureState = FeatureEnumMetaData(KotlinTestFeatures.FOO, FeatureEnum(KotlinTestFeatures.FOO)).defaultFeatureState

        enabledFeatureState.isEnabled shouldBe true
    }

    @Test
    internal fun `should get groups`() {
        val groupsBar = FeatureEnumMetaData(KotlinTestFeatures.BAR, FeatureEnum(KotlinTestFeatures.BAR)).groups

        groupsBar shouldHaveSize 2
        groupsBar.map { it.label } shouldContainAll setOf("Field Level Group Label", "Class Level Group Label")

        val groupsFoo = FeatureEnumMetaData(KotlinTestFeatures.FOO, FeatureEnum(KotlinTestFeatures.FOO)).groups

        groupsFoo shouldHaveSize 1
        groupsFoo.map { it.label } shouldContainAll setOf("Class Level Group Label")
    }

    @Test
    internal fun `should get attributes`() {
        val attributesFoo = FeatureEnumMetaData(KotlinTestFeatures.FOO, FeatureEnum(KotlinTestFeatures.FOO)).attributes

        attributesFoo shouldBe mapOf(Pair("TestAttribute", "fooAttributeValue"))

        val attributesBar = FeatureEnumMetaData(KotlinTestFeatures.BAR, FeatureEnum(KotlinTestFeatures.BAR)).attributes

        attributesBar.size shouldBe 0
    }
}