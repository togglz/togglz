package org.togglz.kotlin

import io.kotlintest.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.togglz.core.Feature
import org.togglz.core.context.StaticFeatureManagerProvider
import org.togglz.kotlin.KFeatureManagerSupport.createFeatureManagerForTest

internal class KFeatureManagerSupportTest {

    @Test
    internal fun `should change toggle state after enable`() {
        KFeatureManagerSupport.allEnabledFeatureConfig(createFeatureManagerForTest(KotlinTestFeatures::class))

        KotlinTestFeatures.BAR.isActive() shouldBe true
        KotlinTestFeatures.FOO.isActive() shouldBe true

        KFeatureManagerSupport.disable(Feature { KotlinTestFeatures.BAR.name })

        KotlinTestFeatures.BAR.isActive() shouldBe false

        KFeatureManagerSupport.enable(Feature { KotlinTestFeatures.BAR.name })
        KotlinTestFeatures.BAR.isActive() shouldBe true
    }

    @Test
    internal fun `should change toggle state after disable`() {
        createFeatureManagerForTest(KotlinTestFeatures::class)
        KFeatureManagerSupport.disable(Feature { KotlinTestFeatures.BAR.name })

        KotlinTestFeatures.BAR.isActive() shouldBe false

        KFeatureManagerSupport.enable(Feature { KotlinTestFeatures.BAR.name })
        KotlinTestFeatures.BAR.isActive() shouldBe true
    }

    @Test
    internal fun `should enable all toggles`() {
        //given
        val featureManager = createFeatureManagerForTest(KotlinTestFeatures::class)
        KFeatureManagerSupport.disable(Feature { KotlinTestFeatures.FOO.name })
        KotlinTestFeatures.FOO.isActive() shouldBe false

        //when
        KFeatureManagerSupport.allEnabledFeatureConfig(featureManager)

        //then
        KotlinTestFeatures.values().forEach { it.isActive() shouldBe true }
    }

    @Test
    internal fun `should disable all toggles`() {
        //given
        val featureManager = createFeatureManagerForTest(KotlinTestFeatures::class)

        KFeatureManagerSupport.enable(Feature { KotlinTestFeatures.FOO.name })
        KotlinTestFeatures.FOO.isActive() shouldBe true

        //when
        KFeatureManagerSupport.allDisabledFeatureConfig(featureManager)

        //then
        KotlinTestFeatures.values().forEach { it.isActive() shouldBe false }
    }

    @AfterEach
    internal fun tearDown() {
        StaticFeatureManagerProvider.setFeatureManager(null)
    }
}