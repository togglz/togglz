package org.togglz.kotlin

import org.togglz.core.Feature
import org.togglz.core.spi.FeatureProvider
import java.util.*
import java.util.Collections.unmodifiableSet

class EnumClassFeatureProvider(featureClass: Class<out Enum<*>>) : FeatureProvider {

    private val features = featureClass.enumConstants.associateWith { FeatureEnum(it) }
    private val metaData = featureClass.enumConstants.associate {
        it.name to FeatureEnumMetaData(it, FeatureEnum(it))
    }

    override fun getFeatures(): Set<Feature> = unmodifiableSet(LinkedHashSet(features.values))

    override fun getMetaData(feature: Feature) = metaData[feature.name()]
}
