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

package org.togglz.spring.boot.legacy.actuate.autoconfigure;

import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.manager.FeatureManager;
import org.togglz.spring.boot.autoconfigure.TogglzAutoConfiguration;
import org.togglz.spring.boot.legacy.actuate.TogglzEndpoint;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Togglz Endpoint (Spring Boot 1.5).
 *
 * @author Rui Figueira
 */
@Configuration
@ConditionalOnClass(AbstractEndpoint.class)
@AutoConfigureAfter(TogglzAutoConfiguration.class)
public class TogglzEndpointAutoConfiguration {

    @Bean
    @ConditionalOnBean(FeatureManager.class)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "togglz.endpoint", name = "enabled", matchIfMissing = true)
    public TogglzEndpoint togglzEndpoint(FeatureManager featureManager) {
        return new TogglzEndpoint(featureManager);
    }
}
