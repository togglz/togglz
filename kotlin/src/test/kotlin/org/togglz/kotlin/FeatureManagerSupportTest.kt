package org.togglz.kotlin

import io.kotlintest.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.togglz.core.Feature
import org.togglz.core.context.StaticFeatureManagerProvider
import org.togglz.kotlin.FeatureManagerSupport.createFeatureManagerForTest

internal class FeatureManagerSupportTest {

    @Test
    internal fun `should change toggle state after enable`() {
        FeatureManagerSupport.enableAllFeatures(createFeatureManagerForTest(KotlinTestFeatures::class))

        KotlinTestFeatures.BAR.isActive() shouldBe true
        KotlinTestFeatures.FOO.isActive() shouldBe true

        FeatureManagerSupport.disable(Feature { KotlinTestFeatures.BAR.name })

        KotlinTestFeatures.BAR.isActive() shouldBe false

        FeatureManagerSupport.enable(Feature { KotlinTestFeatures.BAR.name })
        KotlinTestFeatures.BAR.isActive() shouldBe true
    }

    @Test
    internal fun `should change toggle state after disable`() {
        createFeatureManagerForTest(KotlinTestFeatures::class)
        FeatureManagerSupport.disable(Feature { KotlinTestFeatures.BAR.name })

        KotlinTestFeatures.BAR.isActive() shouldBe false

        FeatureManagerSupport.enable(Feature { KotlinTestFeatures.BAR.name })
        KotlinTestFeatures.BAR.isActive() shouldBe true
    }

    @Test
    internal fun `should enable all toggles`() {
        //given
        val featureManager = createFeatureManagerForTest(KotlinTestFeatures::class)
        FeatureManagerSupport.disable(Feature { KotlinTestFeatures.FOO.name })
        KotlinTestFeatures.FOO.isActive() shouldBe false

        //when
        FeatureManagerSupport.enableAllFeatures(featureManager)

        //then
        KotlinTestFeatures.values().forEach { it.isActive() shouldBe true }
    }

    @Test
    internal fun `should disable all toggles`() {
        //given
        val featureManager = createFeatureManagerForTest(KotlinTestFeatures::class)

        FeatureManagerSupport.enable(Feature { KotlinTestFeatures.FOO.name })
        KotlinTestFeatures.FOO.isActive() shouldBe true

        //when
        FeatureManagerSupport.disableAllFeatures(featureManager)

        //then
        KotlinTestFeatures.values().forEach { it.isActive() shouldBe false }
    }

    @AfterEach
    internal fun tearDown() {
        StaticFeatureManagerProvider.setFeatureManager(null)
    }
}
