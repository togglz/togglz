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

package org.togglz.spring.boot.autoconfigure;

import com.github.heneke.thymeleaf.togglz.TogglzDialect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ManagementContextConfiguration;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.togglz.console.TogglzConsoleServlet;
import org.togglz.core.Feature;
import org.togglz.core.activation.ActivationStrategyProvider;
import org.togglz.core.activation.DefaultActivationStrategyProvider;
import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.manager.EmptyFeatureProvider;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.cache.CachingStateRepository;
import org.togglz.core.repository.composite.CompositeStateRepository;
import org.togglz.core.repository.file.FileBasedStateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.repository.property.PropertyBasedStateRepository;
import org.togglz.core.repository.property.PropertySource;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.UserProvider;
import org.togglz.spring.listener.TogglzApplicationContextBinderApplicationListener;
import org.togglz.spring.security.SpringSecurityUserProvider;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Togglz.
 *
 * @author Marcel Overdijk
 */
@ManagementContextConfiguration
@ConditionalOnProperty(prefix = "togglz", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(TogglzProperties.class)
public class TogglzManagementContextConfiguration {

    @Autowired
    private TogglzProperties properties;

    @Bean
    @Conditional(OnConsoleAndUseManagementPort.class)
    public ServletRegistrationBean togglzConsole() {
        String path = properties.getConsole().getPath();
        String urlMapping = (path.endsWith("/") ? path + "*" : path + "/*");
        TogglzConsoleServlet servlet = new TogglzConsoleServlet();
        servlet.setSecured(properties.getConsole().isSecured());
        return new ServletRegistrationBean(servlet, urlMapping);
    }

    static class OnConsoleAndUseManagementPort extends AllNestedConditions {

        OnConsoleAndUseManagementPort() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(prefix = "togglz.console", name = "enabled", matchIfMissing = true)
        static class OnConsole {
        }

        @ConditionalOnProperty(prefix = "togglz.console", name = "use-management-port", havingValue = "true", matchIfMissing = true)
        static class OnNotUseManagementPort {
        }

    }
}
