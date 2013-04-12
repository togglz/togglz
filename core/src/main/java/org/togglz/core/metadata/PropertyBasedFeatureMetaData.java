package org.togglz.core.metadata;

import java.util.Collections;
import java.util.Set;

import org.togglz.core.group.FeatureGroup;
import org.togglz.core.metadata.FeatureMetaData;

public class PropertyBasedFeatureMetaData implements FeatureMetaData {

	private String label;
	private boolean enabledByDefault;
	private final Set<FeatureGroup> groups;
	
	public PropertyBasedFeatureMetaData(String label, boolean enabledByDefault, Set<FeatureGroup> groups) {
		this.label = label;
		this.enabledByDefault = enabledByDefault;
		this.groups = groups;
	}

	public PropertyBasedFeatureMetaData(String label, boolean enabledByDefault) {
		this(label, enabledByDefault, Collections.<FeatureGroup>emptySet());
	}

	public PropertyBasedFeatureMetaData(String label) {
		this(label, false);
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public boolean isEnabledByDefault() {
		return enabledByDefault;
	}

	@Override
	public Set<FeatureGroup> getGroups() {
		return groups;
	}
	
}
