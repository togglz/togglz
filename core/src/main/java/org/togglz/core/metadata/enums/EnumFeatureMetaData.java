package org.togglz.core.metadata.enums;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.annotation.ActivationParameter;
import org.togglz.core.annotation.DefaultActivationStrategy;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;
import org.togglz.core.metadata.FeatureGroup;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.FeatureAnnotations;

/**
 * Implementation of {@link FeatureMetaData} that looks for annotations like {@link Label}, {@link EnabledByDefault} and
 * {@link DefaultActivationStrategy} on feature enums.
 *
 * @author Christian Kaltepoth
 */
public class EnumFeatureMetaData implements FeatureMetaData {

    private final String label;

    private final FeatureState defaultFeatureState;

    private final Set<FeatureGroup> groups = new HashSet<>();

    private final Map<String, String> attributes = new LinkedHashMap<>();

    public EnumFeatureMetaData(Feature feature) {

        // lookup label via @Label annotation
        this.label = FeatureAnnotations.getLabel(feature);

        // lookup default via @EnabledByDefault
        boolean enabledByDefault = FeatureAnnotations.isEnabledByDefault(feature);
        this.defaultFeatureState = new FeatureState(feature, enabledByDefault);

        // lookup default activation strategy @DefaultActivationStrategy
        DefaultActivationStrategy defaultActivationStrategy = FeatureAnnotations
                .getAnnotation(feature, DefaultActivationStrategy.class);
        if (defaultActivationStrategy != null) {
            this.defaultFeatureState.setStrategyId(defaultActivationStrategy.id());

            for (ActivationParameter parameter : defaultActivationStrategy.parameters()) {
                this.defaultFeatureState.setParameter(parameter.name(), parameter.value());
            }
        }

        // process annotations on the feature
        for (Annotation annotation : FeatureAnnotations.getAnnotations(feature)) {

            FeatureGroup group1 = null;
            if (annotation instanceof org.togglz.core.annotation.FeatureGroup) {
                String annotationValue = ((org.togglz.core.annotation.FeatureGroup) annotation).value();
                group1 = AnnotationFeatureGroup.build(annotation, annotationValue);
                if (group1 != null) {
                    groups.add(group1);
                }
            }
            if(group1 == null) {
                // lookup groups
                FeatureGroup group = AnnotationFeatureGroup.build(annotation.annotationType());
                if (group != null) {
                    groups.add(group);
                }
            }

            // check if this annotation is a feature attribute
            String[] attribute = FeatureAnnotations.getFeatureAttribute(annotation);
            if (attribute != null) {
                attributes.put(attribute[0], attribute[1]);
            }
        }
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public FeatureState getDefaultFeatureState() {
        return defaultFeatureState.copy();
    }

    @Override
    public Set<FeatureGroup> getGroups() {
        return groups;
    }

    @Override
    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

}
