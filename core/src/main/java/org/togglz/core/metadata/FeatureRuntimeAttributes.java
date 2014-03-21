package org.togglz.core.metadata;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Runtime attributes not related to a {@link org.togglz.core.user.FeatureUser} Used to decide the activation of a feature in a
 * {@link org.togglz.core.spi.ActivationStrategy}
 * 
 * @author Fabien Chaillou
 */
public class FeatureRuntimeAttributes {

    private final Map<String, Object> attributes;

    public static Builder builder() {
        return new Builder();
    }

    public FeatureRuntimeAttributes() {
        this.attributes = new LinkedHashMap<String, Object>();
    }

    protected FeatureRuntimeAttributes(Map<String, Object> attributes) {
        this.attributes = new LinkedHashMap<String, Object>(attributes);
    }

    public void addAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public <T> T getAttribute(String name, Class<T> attributeClass) {
        return attributeClass.cast(getAttribute(name));
    }

    private static class Builder {
        private final Map<String, Object> attributes = new LinkedHashMap<String, Object>();

        public Builder withAttribute(String name, Object value) {
            attributes.put(name, value);
            return this;
        }

        public FeatureRuntimeAttributes build() {
            FeatureRuntimeAttributes featureRuntimeAttributes = new FeatureRuntimeAttributes(attributes);
            attributes.clear();
            return featureRuntimeAttributes;
        }
    }

}
