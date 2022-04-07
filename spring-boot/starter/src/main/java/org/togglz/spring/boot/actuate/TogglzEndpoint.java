/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.togglz.spring.boot.actuate;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.Preconditions;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzFeature;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.commaDelimitedListToSet;

/**
 * Spring Boot 2+ {@link Endpoint} to expose Togglz info as an actuator endpoint.
 * This endpoint is exposed as Spring Boot Actuator endpoint. It allows the user to get an overview of
 * all available toggles without adding the togglz-console dependency.
 * The user can also read the state of specific toggles and even enable or disable specific toggles.
 *
 * @author Rui Figueira
 */
@Component
@Endpoint(id = "togglz")
public class TogglzEndpoint {

    private static final String PARAMETER_VALUE_SEPARATOR = "=";

    private final FeatureManager featureManager;

    public TogglzEndpoint(FeatureManager featureManager) {
        Assert.notNull(featureManager, "FeatureManager must not be null");
        this.featureManager = featureManager;
    }

    @ReadOperation
    public List<TogglzFeature> getAllFeatures() {
        List<TogglzFeature> features = new ArrayList<>();
        for (Feature feature : this.featureManager.getFeatures()) {
            FeatureState featureState = this.featureManager.getFeatureState(feature);
            features.add(new TogglzFeature(feature, featureState));
        }
        Collections.sort(features);
        return features;
    }

    @ReadOperation
    public TogglzFeature getFeature(@Selector String name) {
        return this.featureManager.getFeatures().stream()
            .filter(it -> name.equals(it.name()))
            .findFirst()
            .map(it -> new TogglzFeature(it, this.featureManager.getFeatureState(it)))
            .orElse(null);
    }

    /**
     * Allows to change the state of toggles via http post.
     *
     * @param name    the name of the toggle/feature
     * @param enabled the name of the field containing the toggle/feature status
     * @param strategy the ID of the activation strategy to use
     * @param parameters activation strategy parameters as comma separated list of key=value pairs
     */
    @WriteOperation
    public TogglzFeature setFeatureState(@Selector String name, @Nullable Boolean enabled,
                                         @Nullable String strategy, @Nullable String parameters) {
        final Feature feature = featureManager.getFeatures().stream()
                .filter(f -> f.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Could not find feature with name " + name));

        Map<String, String> parametersMap = parameters != null ?
                commaDelimitedListToSet(parameters).stream()
                        .map(this::toParameterKeyValue)
                        .collect(Collectors.toMap(
                                parameterKeyValue -> parameterKeyValue[0],
                                parameterKeyValue -> parameterKeyValue[1]))
                : Collections.emptyMap();

        FeatureState featureState = changeFeatureStatus(feature, enabled, strategy, parametersMap);

        return new TogglzFeature(feature, featureState);
    }

    private FeatureState changeFeatureStatus(
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
