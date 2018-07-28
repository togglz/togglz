package org.togglz.core.manager;

import java.util.Arrays;

import org.togglz.core.Feature;
import org.togglz.core.GenericEnumFeature;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.metadata.enums.GenericEnumFeatureMetaData;
import org.togglz.core.spi.FeatureProvider;

/**
 * Implementation of {@link FeatureProvider} that uses generic java enum
 * (not implementing {@link Feature}) to represent features.
 *
 * @author Rui Figueira
 */
public class GenericEnumBasedFeatureProvider extends AbstractEnumBasedFeatureProvider<Enum<?>> {

    public GenericEnumBasedFeatureProvider() {
        // nothing to do
    }

    @SafeVarargs
    public GenericEnumBasedFeatureProvider(Class<? extends Enum<?>>... featureEnums) {
        if (featureEnums == null) {
            throw new IllegalArgumentException("The featureEnums argument must not be null");
        }
        for (Class<? extends Enum<?>> featureEnum : featureEnums) {
            addFeatureEnum(featureEnum);
        }
    }

    public GenericEnumBasedFeatureProvider addFeatureEnum(Class<? extends Enum<?>> featureEnum) {
        if (featureEnum == null) {
            throw new IllegalArgumentException("The featureEnum argument must be an enum");
        }
        addFeatures(Arrays.asList(featureEnum.getEnumConstants()));
        return this;
    }

    @Override
    protected Feature createFeatureFor(Enum<?> enumValue) {
        return new GenericEnumFeature(enumValue);
    }

    @Override
    protected FeatureMetaData featureMetaDataFor(Enum<?> enumValue, Feature feature) {
        return new GenericEnumFeatureMetaData(enumValue, feature);
    }
}
