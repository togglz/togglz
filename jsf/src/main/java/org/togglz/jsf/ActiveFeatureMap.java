package org.togglz.jsf;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;

public class ActiveFeatureMap implements Map<String, Boolean> {

    @Override
    public Boolean get(Object key) {

        FeatureManager featureManager = FeatureContext.getFeatureManager();

        // the name of the feature
        String name = (key != null) ? key.toString() : "null";

        // get the correct feature
        Feature feature = null;
        for (Feature f : featureManager.getFeatures()) {
            if (f.name().equals(name)) {
                feature = f;
                break;
            }
        }

        // we need a matching feature
        if (feature == null) {
            throw new IllegalArgumentException("Unknown feature: " + name);
        }

        return featureManager.isActive(feature);

    }

    @Override
    public int size() {
        return FeatureContext.getFeatureManager().getFeatures().length;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /*
     * Unsupported operations
     */

    @Override
    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean put(String key, Boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends Boolean> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Boolean> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<java.util.Map.Entry<String, Boolean>> entrySet() {
        throw new UnsupportedOperationException();
    }

}
