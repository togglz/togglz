package org.togglz.core.util.annotations;

import org.togglz.core.Feature;

/**
 * Annotations processor class to read annotation on feature enums.
 *
 * @author Rui Figueira
 */
public class FeatureAnnotationsProcessor extends AbstractAnnotationsProcessor<Feature> {

    public static final FeatureAnnotationsProcessor INSTANCE = new FeatureAnnotationsProcessor();

    @Override
    protected String getName(Feature feature) {
        return feature.name();
    }
}
