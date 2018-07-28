package org.togglz.core.metadata.enums;

import org.togglz.core.Feature;
import org.togglz.core.util.annotations.GenericEnumAnnotationsProcessor;

/**
 * Implementation of {@link AbstractEnumFeatureMetaData} specific for generic enums.
 *
 * @author Rui Figueira
 */
public class GenericEnumFeatureMetaData extends AbstractEnumFeatureMetaData<Enum<?>> {

    public GenericEnumFeatureMetaData(Enum<?> enumValue, Feature feature) {
        super(enumValue, feature, GenericEnumAnnotationsProcessor.INSTANCE);
    }

}
