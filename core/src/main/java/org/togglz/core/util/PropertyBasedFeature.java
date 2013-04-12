package org.togglz.core.util;

import java.util.HashSet;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.group.FeatureGroup;
import org.togglz.core.group.PropertyBasedFeatureGroup;
import org.togglz.core.metadata.EmptyFeatureMetaData;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.metadata.PropertyBasedFeatureMetaData;

public class PropertyBasedFeature implements Feature {

	private final String name;
	private final String value;
	private FeatureMetaData metaData;

	public PropertyBasedFeature(String name, String value) {
		this.name = name;
		this.value = value;
		Validate.notBlank(name, "name is required");
		parse();
	}

	private void parse() {
		if (value != null) {
			String[] parts = value.split(";");
			if (parts.length == 3) {
				String label = parts[0];
				boolean enabledByDefault = Boolean.parseBoolean(parts[1]);
				Set<FeatureGroup> groups = parseFeatureGroups(parts[2]);
				metaData = new PropertyBasedFeatureMetaData(label,
						enabledByDefault, groups);
			} else if (parts.length == 2) {
				String label = parts[0];
				boolean enabledByDefault = Boolean.parseBoolean(parts[1]);
				metaData = new PropertyBasedFeatureMetaData(label,
						enabledByDefault);
			} else if (parts.length == 1) {
				String label = parts[0];
				metaData = new PropertyBasedFeatureMetaData(label);
			} else {
				metaData = new EmptyFeatureMetaData(this);
			}
		}

	}

	private Set<FeatureGroup> parseFeatureGroups(String value) {
		String[] parts = value.split(",");
		Set<FeatureGroup> groups = new HashSet<FeatureGroup>();
		for (String label : parts) {
			groups.add(new PropertyBasedFeatureGroup(label, name));
		}
		return groups;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public boolean isActive() {
		return metaData.isEnabledByDefault();
	}

	public FeatureMetaData getMetaData() {
		return metaData;
	}

}
