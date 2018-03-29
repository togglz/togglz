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

package org.togglz.spring.boot1.autoconfigure.actuator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;
import org.togglz.spring.boot.autoconfigure.TogglzFeature;

/**
 * {@link Endpoint} to expose Togglz info.
 *
 * @author Marcel Overdijk
 */
@ConfigurationProperties(prefix = "togglz.endpoint", ignoreUnknownFields = true)
public class TogglzEndpoint extends AbstractEndpoint<List<TogglzFeature>> {

    private final FeatureManager featureManager;

    public TogglzEndpoint(FeatureManager featureManager) {
        super("togglz");
        Assert.notNull(featureManager, "FeatureManager must not be null");
        this.featureManager = featureManager;
    }

    @Override
    public List<TogglzFeature> invoke() {
        List<TogglzFeature> features = new ArrayList<>();
        for (Feature feature : this.featureManager.getFeatures()) {
            FeatureState featureState = this.featureManager.getFeatureState(feature);
            features.add(new TogglzFeature(feature, featureState));
        }
        Collections.sort(features);
        return features;
    }
}
