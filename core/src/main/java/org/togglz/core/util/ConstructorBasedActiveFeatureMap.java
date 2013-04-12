package org.togglz.core.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;

/**
 * A Map facade that allows to determine if a named feature is active.  
 * The map allows constructor-based injection of the {@link FeatureManager} for use in DI containers.
 * 
 * @author Mauro Talevi
 */
public class ConstructorBasedActiveFeatureMap implements Map<String,Boolean> {

	private final FeatureManager manager;
	
	public ConstructorBasedActiveFeatureMap(FeatureManager manager) {
		this.manager = manager;
	}

	@Override
	public int size() {
		return manager.getFeatures().size();
	}

	@Override
	public boolean isEmpty() {
		return manager.getFeatures().isEmpty();
	}

	@Override
	public Boolean get(Object key) {
		Feature feature = feature(key);		
		return feature != null ? manager.isActive(feature) : false;
	}

	// This should be replaced by a FeatureManager.getFeature(String name) method.
	private Feature feature(Object key) {
		String name = (String)key;
		for ( Feature feature : manager.getFeatures() ){
			if ( feature.name().equals(name) ){
				return feature;
			}
		}
		return null;
	}

	// Unsupported operations	

	@Override
	public boolean containsKey(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Boolean remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Boolean put(String key, Boolean value) {
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
	public Set<Entry<String, Boolean>> entrySet() {
		throw new UnsupportedOperationException();
	}

}
