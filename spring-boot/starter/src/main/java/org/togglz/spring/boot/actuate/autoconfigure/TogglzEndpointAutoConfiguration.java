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

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.context.ConfigurableWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.manager.FeatureManager;
import org.togglz.spring.boot.actuate.TogglzEndpoint;
import org.togglz.spring.listener.TogglzApplicationContextBinderApplicationListener;
import org.togglz.spring.listener.TogglzApplicationContextBinderApplicationListener.ContextRefreshedEventFilter;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Togglz Endpoint (Spring Boot 2.3.x).
 *
 * @author Rui Figueira
 */
@Configuration
@ConditionalOnClass(Endpoint.class)
@AutoConfigureAfter(TogglzAutoConfiguration.class)
public class TogglzEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(TogglzApplicationContextBinderApplicationListener.class)
    ContextRefreshedEventFilter contextRefreshedEventFilter() {
        return contextRefreshedEvent -> {
            ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
            if (applicationContext instanceof ConfigurableWebServerApplicationContext) {
                return ((ConfigurableWebServerApplicationContext) applicationContext).getServerNamespace() == null;
            }
            return false;
        };
    }

    @Bean
    @ConditionalOnBean(FeatureManager.class)
    @ConditionalOnMissingBean
    public TogglzEndpoint togglzEndpoint(FeatureManager featureManager) {
        return new TogglzEndpoint(featureManager);
    }
}
