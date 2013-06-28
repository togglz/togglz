package org.togglz.core.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.metadata.property.PropertyFeatureMetaData;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.util.NamedFeature;

/**
 * <p>
 * Implementation of {@link FeatureProvider} that reads all the feature specification from a {@link Properties} instance. This
 * provider is especially useful in distributed environments.
 * </p>
 * 
 * <p>
 * The feature specification has the following format:
 * </p>
 * 
 * <pre>
 * &lt;feature&gt;=&lt;label&gt;;&lt;enabledByDefault&gt;[;&lt;group1&gt;,&lt;group2&gt;,...]
 * </pre>
 * 
 * <p>
 * The following example shows how a concrete specification could look like
 * </p>
 * 
 * <pre>
 * FEATURE_ONE=A useful feature;true;Group 1,Group 3
 * FEATURE_TWO=Some other feature;false;Group 2
 * </pre>
 * 
 * @author Mauro Talevi
 * @author Christian Kaltepoth
 * 
 */
public class PropertyFeatureProvider implements FeatureProvider {

    private final Set<Feature> features = new LinkedHashSet<Feature>();

    private final Map<String, FeatureMetaData> metadata = new HashMap<String, FeatureMetaData>();

    public PropertyFeatureProvider(Properties properties) {

        for (Entry<Object, Object> entry : properties.entrySet()) {

            String name = entry.getKey().toString();
            String spec = entry.getValue().toString();

            NamedFeature feature = new NamedFeature(name);

            features.add(feature);
            metadata.put(name, new PropertyFeatureMetaData(feature, spec));

        }

    }

    @Override
    public Set<Feature> getFeatures() {
        return Collections.unmodifiableSet(features);
    }

    @Override
    public FeatureMetaData getMetaData(Feature feature) {
        return metadata.get(feature.name());
    }

}
