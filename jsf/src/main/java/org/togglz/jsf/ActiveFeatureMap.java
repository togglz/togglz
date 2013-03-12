package org.togglz.jsf;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.togglz.core.context.FeatureContext;
import org.togglz.core.util.UntypedFeature;
import org.togglz.core.util.Validate;

public class ActiveFeatureMap implements Map<String, Boolean> {

    @Override
    public Boolean get(Object key) {
        Validate.notNull(key, "The feature must not be null");
        return new UntypedFeature(key.toString()).isActive();
    }

    @Override
    public int size() {
        return FeatureContext.getFeatureManager().getFeatures().size();
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
