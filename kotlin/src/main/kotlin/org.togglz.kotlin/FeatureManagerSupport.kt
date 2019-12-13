package org.togglz.kotlin

import org.togglz.core.Feature
import org.togglz.core.context.FeatureContext.clearCache
import org.togglz.core.context.FeatureContext.getFeatureManager
import org.togglz.core.context.StaticFeatureManagerProvider
import org.togglz.core.manager.FeatureManager
import org.togglz.core.manager.FeatureManagerBuilder
import org.togglz.core.repository.FeatureState
import kotlin.reflect.KClass

object FeatureManagerSupport {

    fun createFeatureManagerForTest(featureClass: KClass<out Enum<*>>): FeatureManager {
        val featureManager = FeatureManagerBuilder.begin()
            .featureProvider(EnumClassFeatureProvider(featureClass.java))
            .build()
        StaticFeatureManagerProvider.setFeatureManager(featureManager)
        return featureManager
    }

    fun enableAllFeatures(featureManager: FeatureManager) {
        for (feature in featureManager.features) {
            if (shouldRunInTests(feature, featureManager)) {
                featureManager.setFeatureState(FeatureState(feature, true))
            }
        }
        clearCache()
    }

    fun disableAllFeatures(featureManager: FeatureManager) {
        for (feature in featureManager.features) {
            featureManager.setFeatureState(FeatureState(feature, false))
        }
        clearCache()
    }

    fun enable(feature: Feature) {
        getFeatureManager().setFeatureState(FeatureState(feature, true))
    }

    fun disable(feature: Feature) {
        getFeatureManager().setFeatureState(FeatureState(feature, false))
    }

    private fun shouldRunInTests(feature: Feature, featureManager: FeatureManager): Boolean {
        val label = featureManager.getMetaData(feature).label
        return !label.contains("[inactiveInTests]")
    }

}
