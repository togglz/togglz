package org.togglz.kotlin

import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

internal class EnumClassFeatureProviderTest {

    private val featureProvider = EnumClassFeatureProvider(KotlinTestFeatures::class.java)

    @Test
    internal fun `should return wrapped features`() {
        val features = featureProvider.features

        features shouldContainAll setOf(FeatureEnum(KotlinTestFeatures.BAR), FeatureEnum(KotlinTestFeatures.FOO))
    }

    @Test
    internal fun `should get metadata`() {
        val metaData = featureProvider.getMetaData(FeatureEnum(KotlinTestFeatures.FOO))

        val expectedMetaData = FeatureEnumMetaData(KotlinTestFeatures.FOO, FeatureEnum(KotlinTestFeatures.FOO))
        metaData!!.label shouldBe expectedMetaData.label
        metaData.attributes shouldBe expectedMetaData.attributes
        metaData.groups.map { it.label } shouldContainAll expectedMetaData.groups.map { it.label }
        metaData.defaultFeatureState.isEnabled shouldBe expectedMetaData.defaultFeatureState.isEnabled
    }
}