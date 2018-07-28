package org.togglz.core.manager;

import java.util.Arrays;

import org.togglz.core.Feature;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.metadata.enums.EnumFeatureMetaData;
import org.togglz.core.spi.FeatureProvider;

/**
 * Implementation of {@link FeatureProvider} that uses an Java enum to represent features.
 *
 * @author Christian Kaltepoth
 */
public class EnumBasedFeatureProvider extends AbstractEnumBasedFeatureProvider<Feature> {

    public EnumBasedFeatureProvider() {
        // nothing to do
    }

    public EnumBasedFeatureProvider(@SuppressWarnings("unchecked") Class<? extends Feature>... featureEnums) {
        if (featureEnums == null) {
            throw new IllegalArgumentException("The featureEnums argument must not be null");
        }
        for (Class<? extends Feature> featureEnum : featureEnums) {
            addFeatureEnum(featureEnum);
        }
    }

    public EnumBasedFeatureProvider addFeatureEnum(Class<? extends Feature> featureEnum) {
        if (featureEnum == null || !featureEnum.isEnum()) {
            throw new IllegalArgumentException("The featureEnum argument must be an enum");
        }
        addFeatures(Arrays.asList(featureEnum.getEnumConstants()));
        return this;
    }

    @Override
    protected Feature createFeatureFor(Feature enumValue) {
        return enumValue;
    }

    @Override
    protected FeatureMetaData featureMetaDataFor(Feature enumValue, Feature feature) {
        return new EnumFeatureMetaData(feature);
    }
}
