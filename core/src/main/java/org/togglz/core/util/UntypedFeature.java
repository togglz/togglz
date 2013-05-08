package org.togglz.core.util;

import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;

/**
 * An untyped feature can be used if the name of a feature is known but not the correct type of the feature enum. The real type
 * of the feature will be resolved lazily by asking the {@link FeatureManager} for the features it is responsible for. If the
 * name of the untyped feature doesn't correspond to one of these features, a runtime exception will be thrown.
 * 
 * @author Christian Kaltepoth
 * 
 * @deprecated Use {@link NamedFeature} instead.
 */
@Deprecated
public class UntypedFeature implements Feature {

    private final String name;

    public UntypedFeature(String name) {
        Validate.notBlank(name, "name is required");
        this.name = name;
    }

    @Override
    public String name() {
        return getTypedFeature().name();
    }

    private Feature _feature;

    private Feature getTypedFeature() {
        if (_feature == null) {
            Set<Feature> features = FeatureContext.getFeatureManager().getFeatures();
            for (Feature f : features) {
                if (f.name().equals(name)) {
                    _feature = f;
                }
            }
            if (_feature == null) {
                throw new IllegalArgumentException("FeatureManager doesn't know about feature: " + name);
            }
        }
        return _feature;
    }

}
