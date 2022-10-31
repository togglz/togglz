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

import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.Feature;
import org.togglz.core.activation.Parameter;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.FeatureUser;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzAutoConfiguration;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzEndpointAutoConfiguration;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzManagementContextConfiguration;

public class BaseTest {

    protected final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ManagementContextAutoConfiguration.class,
                    EndpointAutoConfiguration.class,
                    WebEndpointAutoConfiguration.class,
                    TogglzAutoConfiguration.class,
                    TogglzEndpointAutoConfiguration.class,
                    TogglzManagementContextConfiguration.class
                    ));

    protected enum MyFeatures implements Feature {
        FEATURE_ONE,
        FEATURE_TWO;
    }

    protected static class CustomActivationStrategy implements ActivationStrategy {

        @Override
        public String getId() {
            return "custom";
        }

        @Override
        public String getName() {
            return "Custom";
        }

        @Override
        public boolean isActive(FeatureState featureState, FeatureUser user) {
            return true;
        }

        @Override
        public Parameter[] getParameters() {
            return new Parameter[0];
        }
    }

    @Configuration
    protected static class DispatcherServletPathConfig {
        @Bean
        public DispatcherServletPath dispatcherServletPath() {
            return () -> "";
        }
    }

    @Configuration
    protected static class FeatureProviderConfig {

        @SuppressWarnings("unchecked")
    	@Bean
        public FeatureProvider featureProvider() {
            return new EnumBasedFeatureProvider(MyFeatures.class);
        }
    }

    @Configuration
    protected static class ActivationStrategyConfig {

        @Bean
        public CustomActivationStrategy customActivationStrategy() {
            return new CustomActivationStrategy();
        }
    }

    protected WebApplicationContextRunner contextRunnerWithFeatureProviderConfig() {
        return contextRunner
            .withPropertyValues(
                    "togglz.console.enabled: true",
                    "togglz.console.use-management-port: true")
            .withUserConfiguration(FeatureProviderConfig.class)
            .withUserConfiguration(DispatcherServletPathConfig.class);
    }

}