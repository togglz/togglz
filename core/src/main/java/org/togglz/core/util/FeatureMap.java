package org.togglz.core.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.LazyResolvingFeatureManager;

/**
 * <p>
 * This map can be used to check whether features are active. You can use either a {@link Feature} or a feature name as the key
 * for a map lookup. The resulting boolean will indicate if the feature is active or not.
 * </p>
 * 
 * <p>
 * The map allows constructor-based injection of the {@link FeatureManager} for use in DI containers.
 * </p>
 * 
 * @author Mauro Talevi
 * @author Christian Kaltepoth
 */
public class FeatureMap implements Map<Object, Boolean> {

    private final FeatureManager manager;

    /**
     * Constructor that will configure the map to lazily lookup the {@link FeatureManager} from the {@link FeatureContext}.
     */
    public FeatureMap() {
        this(new LazyResolvingFeatureManager());
    }

    /**
     * Constructor that allows to manually set the feature manager to use.
     */
    public FeatureMap(FeatureManager manager) {
        this.manager = manager;
    }

    @Override
    public Boolean get(Object key) {

        Validate.notNull(key, "The feature must not be null");

        Feature feature = null;
        if (key instanceof Feature) {
            feature = (Feature) key;
        } else {
            feature = new NamedFeature(key.toString());
        }

        return manager.isActive(feature);
    }

    @Override
    public int size() {
        return manager.getFeatures().size();
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
    public Boolean put(Object key, Boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends Object, ? extends Boolean> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Object> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Boolean> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Map.Entry<Object, Boolean>> entrySet() {
        throw new UnsupportedOperationException();
    }

}
