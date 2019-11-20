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

package org.togglz.spring.boot.actuate.autoconfigure;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import java.util.Map;

/**
 * Data Transfer Object for Togglz feature information.
 *
 * @author Marcel Overdijk
 * @author Rui Figueira
 */
public class TogglzFeature implements Comparable<TogglzFeature> {

    private String name;
    private boolean enabled;
    private String strategy;
    private Map<String, String> params;

    public TogglzFeature(Feature feature, FeatureState featureState) {
        this.name = feature.name();
        this.enabled = featureState.isEnabled();
        this.strategy = featureState.getStrategyId();
        this.params = featureState.getParameterMap();
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getStrategy() {
        return strategy;
    }

    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public int compareTo(TogglzFeature o) {
        return name.compareTo(o.getName());
    }
}