package org.togglz.core.metadata.enums;

import org.togglz.core.Feature;
import org.togglz.core.util.annotations.FeatureAnnotationsProcessor;

/**
 *
 * Implementation of {@link AbstractEnumFeatureMetaData} specific for feature enums.
 *
 * @author Christian Kaltepoth
 *
 */
public class EnumFeatureMetaData extends AbstractEnumFeatureMetaData<Feature> {

    public EnumFeatureMetaData(Feature feature) {
        super(feature, feature, FeatureAnnotationsProcessor.INSTANCE);
    }
}
