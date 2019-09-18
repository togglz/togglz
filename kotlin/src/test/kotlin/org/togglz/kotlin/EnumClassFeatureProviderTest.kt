package org.togglz.kotlin

import org.junit.jupiter.api.Test

internal class EnumClassFeatureProviderTest {

    @Test
    internal fun name() {
        val features = EnumClassFeatureProvider(KotlinTestFeatures::class.java).features

        println(features)
    }
}