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
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzFeature;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzFeatureMetaData;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
public class TogglzEndpoint extends AbstractTogglzEndpoint {


    public TogglzEndpoint(FeatureManager featureManager) {
        super(featureManager);
    }

    @ReadOperation
    public List<TogglzFeature> getAllFeatures() {
        return this.featureManager.getFeatures().stream()
                .map(this::generateTogglzFeature)
                .sorted()
                .collect(Collectors.toList());
    }

    @ReadOperation
    public TogglzFeature getFeature(@Selector String name) {
        return this.featureManager.getFeatures().stream()
                .filter(it -> name.equals(it.name()))
                .findFirst()
                .map(this::generateTogglzFeature)
                .orElse(null);
    }


    /**
     * Allows to change the state of toggles via http post.
     *
     * @param name       the name of the toggle/feature
     * @param enabled    the name of the field containing the toggle/feature status
     * @param strategy   the ID of the activation strategy to use
     * @param parameters activation strategy parameters as comma separated list of key=value pairs
     */
    @WriteOperation
    public TogglzFeature setFeatureState(@Selector String name, @Nullable Boolean enabled,
                                         @Nullable String strategy, @Nullable String parameters) {
        final Feature feature = findFeature(name);
        if (feature == null) {
            throw new IllegalArgumentException("Could not find feature with name " + name);
        }
        Map<String, String> parametersMap = parseParameterMap(parameters);
        FeatureState featureState = changeFeatureStatus(feature, enabled, strategy, parametersMap);
        FeatureMetaData metaData = this.featureManager.getMetaData(feature);
        return new TogglzFeature(feature, featureState, new TogglzFeatureMetaData(metaData));
    }

}

