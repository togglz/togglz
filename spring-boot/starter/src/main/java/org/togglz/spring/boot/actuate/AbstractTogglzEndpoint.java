package org.togglz.spring.boot.actuate;

import org.springframework.util.Assert;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.Preconditions;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzFeature;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzFeatureMetaData;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.commaDelimitedListToSet;

public abstract class AbstractTogglzEndpoint {

    private static final String PARAMETER_VALUE_SEPARATOR = "=";

    protected final FeatureManager featureManager;

    public AbstractTogglzEndpoint(FeatureManager featureManager) {
        Assert.notNull(featureManager, "FeatureManager must not be null");
        this.featureManager = featureManager;
    }

    protected Feature findFeature(String name) {
        return featureManager.getFeatures().stream()
                .filter(f -> f.name().equals(name))
                .findFirst().orElse(null);
    }


    protected TogglzFeature generateTogglzFeature(Feature feature) {
        FeatureState featureState = this.featureManager.getFeatureState(feature);
        FeatureMetaData featureMetaData = this.featureManager.getMetaData(feature);
        return new TogglzFeature(feature, featureState, new TogglzFeatureMetaData(featureMetaData));
    }

    protected Map<String, String> parseParameterMap(String parameters) {
        return parameters != null ?
                commaDelimitedListToSet(parameters).stream()
                        .map(this::toParameterKeyValue)
                        .collect(Collectors.toMap(
                                parameterKeyValue -> parameterKeyValue[0],
                                parameterKeyValue -> parameterKeyValue[1]))
                : Collections.emptyMap();
    }


    protected FeatureState changeFeatureStatus(
            Feature feature, Boolean enabled, String strategy, Map<String, String> parameters) {
        FeatureState featureState = featureManager.getFeatureState(feature);

        if (enabled != null) {
            featureState.setEnabled(enabled);
        }
        if (strategy != null) {
            featureState.setStrategyId(strategy);
        }
        parameters.forEach(featureState::setParameter);

        featureManager.setFeatureState(featureState);

        return featureState;
    }

    private String[] toParameterKeyValue(String parameterString) {
        String[] parameterKeyValue = parameterString.split(PARAMETER_VALUE_SEPARATOR);

        Preconditions.checkArgument(
                parameterKeyValue.length == 2,
                "Illegal parameter key/value format: %s", parameterString);

        return Arrays.stream(parameterKeyValue).map(String::trim).toArray(String[]::new);
    }

}
