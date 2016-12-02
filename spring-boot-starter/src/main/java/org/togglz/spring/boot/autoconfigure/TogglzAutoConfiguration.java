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
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.context.annotation.Bean;
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
@Configuration
@ConditionalOnProperty(prefix = "togglz", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(TogglzProperties.class)
public class TogglzAutoConfiguration {

    private static final Log log = LogFactory.getLog(TogglzAutoConfiguration.class);

    @Bean
    public TogglzApplicationContextBinderApplicationListener togglzApplicationContextBinderApplicationListener() {
        return new TogglzApplicationContextBinderApplicationListener();
    }

    @Configuration
    @ConditionalOnMissingBean(FeatureProvider.class)
    protected static class FeatureProviderConfiguration {

        @Autowired
        private TogglzProperties properties;

        @Bean
        public FeatureProvider featureProvider() {
            Class<? extends Feature>[] featureEnums = properties.getFeatureEnums();
            if (featureEnums != null && featureEnums.length > 0) {
                return new EnumBasedFeatureProvider(featureEnums);
            } else {
                log.warn("Creating a dummy feature provider as neither a FeatureProvider bean was provided nor the 'togglz.feature-enums' property was set!");
                return new EmptyFeatureProvider();
            }
        }
    }

    @Configuration
    @ConditionalOnMissingBean(FeatureManager.class)
    protected static class FeatureManagerConfiguration {

        @Autowired
        private TogglzProperties properties;

        @Bean
        public FeatureManager featureManager(FeatureProvider featureProvider, List<StateRepository> stateRepositories, UserProvider userProvider, ActivationStrategyProvider activationStrategyProvider) {
            StateRepository stateRepository = null;
            if (stateRepositories.size() == 1) {
                stateRepository = stateRepositories.get(0);
            } else if (stateRepositories.size() > 1) {
                stateRepository = new CompositeStateRepository(stateRepositories.toArray(new StateRepository[stateRepositories.size()]));
            }
            // If caching is enabled wrap state repository in caching state repository.
            // Note that we explicitly check if the state repository is not already a caching state repository,
            // as the auto configuration of the state repository already creates a caching state repository if needed.
            // The below wrapping only occurs if the user provided the state repository manually and caching is enabled.
            if (properties.getCache().isEnabled() && !(stateRepository instanceof CachingStateRepository)) {
                stateRepository = new CachingStateRepository(stateRepository, properties.getCache().getTimeToLive(), properties.getCache().getTimeUnit());
            }
            FeatureManagerBuilder featureManagerBuilder = new FeatureManagerBuilder();
            String name = properties.getFeatureManagerName();
            if (name != null && name.length() > 0) {
                featureManagerBuilder.name(name);
            }
            featureManagerBuilder
                    .featureProvider(featureProvider)
                    .stateRepository(stateRepository)
                    .userProvider(userProvider)
                    .activationStrategyProvider(activationStrategyProvider)
                    .build();
            return featureManagerBuilder.build();
        }
    }

    @Configuration
    @ConditionalOnMissingBean(ActivationStrategyProvider.class)
    protected static class ActivationStrategyProviderConfiguration {

        @Autowired(required = false)
        private List<ActivationStrategy> activationStrategies;

        @Bean
        public ActivationStrategyProvider activationStrategyProvider() {
            DefaultActivationStrategyProvider provider = new DefaultActivationStrategyProvider();
            if (activationStrategies != null && activationStrategies.size() > 0) {
                provider.addActivationStrategies(activationStrategies);
            }
            return provider;
        }
    }

    @Configuration
    @ConditionalOnMissingBean(StateRepository.class)
    protected static class StateRepositoryConfiguration {

        @Autowired
        private ResourceLoader resourceLoader = new DefaultResourceLoader();

        @Autowired
        private TogglzProperties properties;

        @Bean
        public StateRepository stateRepository() throws IOException {
            StateRepository stateRepository;
            Map<String, String> features = properties.getFeatures();
            String featuresFile = properties.getFeaturesFile();
            if (featuresFile != null) {
                Resource resource = this.resourceLoader.getResource(featuresFile);
                Integer minCheckInterval = properties.getFeaturesFileMinCheckInterval();
                if (minCheckInterval != null) {
                    stateRepository = new FileBasedStateRepository(resource.getFile(), minCheckInterval);
                } else {
                    stateRepository = new FileBasedStateRepository(resource.getFile());
                }
            } else if (features != null && features.size() > 0) {
                Properties props = new Properties();
                props.putAll(features);
                PropertySource propertySource = new PropertiesPropertySource(props);
                stateRepository = new PropertyBasedStateRepository(propertySource);
            } else {
                stateRepository = new InMemoryStateRepository();
            }
            // If caching is enabled wrap state repository in caching state repository.
            if (properties.getCache().isEnabled()) {
                stateRepository = new CachingStateRepository(stateRepository, properties.getCache().getTimeToLive(), properties.getCache().getTimeUnit());
            }
            return stateRepository;
        }
    }

    @Configuration
    @ConditionalOnMissingClass("org.springframework.security.config.annotation.web.configuration.EnableWebSecurity")
    @ConditionalOnMissingBean(UserProvider.class)
    protected static class UserProviderConfiguration {

        @Autowired
        private TogglzProperties properties;

        @Bean
        public UserProvider userProvider() {
            return new NoOpUserProvider();
        }
    }

    @Configuration
    @ConditionalOnClass({EnableWebSecurity.class, AuthenticationEntryPoint.class, SpringSecurityUserProvider.class})
    @ConditionalOnMissingBean(UserProvider.class)
    protected static class SpringSecurityUserProviderConfiguration {

        @Autowired
        private TogglzProperties properties;

        @Bean
        public UserProvider userProvider() {
            return new SpringSecurityUserProvider(properties.getConsole().getFeatureAdminAuthority());
        }
    }

    @Configuration
    @ConditionalOnWebApplication
    @ConditionalOnClass(TogglzConsoleServlet.class)
    @ConditionalOnProperty(prefix = "togglz.console", name = "enabled", matchIfMissing = true)
    protected static class TogglzConsoleConfiguration {

        @Autowired
        private TogglzProperties properties;

        @Bean
        public ServletRegistrationBean togglzConsole() {
            String path = properties.getConsole().getPath();
            String urlMapping = (path.endsWith("/") ? path + "*" : path + "/*");
            TogglzConsoleServlet servlet = new TogglzConsoleServlet();
            servlet.setSecured(properties.getConsole().isSecured());
            return new ServletRegistrationBean(servlet, urlMapping);
        }
    }

    @Configuration
    @ConditionalOnClass(Endpoint.class)
    @ConditionalOnMissingBean(TogglzEndpoint.class)
    @ConditionalOnProperty(prefix = "togglz.endpoint", name = "enabled", matchIfMissing = true)
    protected static class TogglzEndpointConfiguration {

        @Autowired
        private TogglzProperties properties;

        @Bean
        public TogglzEndpoint togglzEndpoint(FeatureManager featureManager) {
            return new TogglzEndpoint(featureManager);
        }
    }

    @Configuration
    @ConditionalOnClass(TogglzDialect.class)
    protected static class ThymeleafTogglzDialectConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public TogglzDialect togglzDialect() {
            return new TogglzDialect();
        }
    }
}
