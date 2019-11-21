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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.togglz.core.Feature;
import org.togglz.core.activation.ActivationStrategyProvider;
import org.togglz.core.activation.DefaultActivationStrategyProvider;
import org.togglz.core.manager.*;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.cache.CachingStateRepository;
import org.togglz.core.repository.composite.CompositeStateRepository;
import org.togglz.core.repository.file.FileBasedStateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.UserProvider;
import org.togglz.spring.listener.TogglzApplicationContextBinderApplicationListener;
import org.togglz.spring.listener.TogglzApplicationContextBinderApplicationListener.ContextRefreshedEventFilter;
import org.togglz.spring.security.SpringSecurityUserProvider;
import org.togglz.spring.web.FeatureInterceptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Togglz.
 *
 * @author Marcel Overdijk
 */
@Configuration
@ConditionalOnProperty(prefix = "togglz", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(TogglzProperties.class)
public class TogglzAutoConfiguration {

    @Bean
    public TogglzApplicationContextBinderApplicationListener togglzApplicationContextBinderApplicationListener(
        ObjectProvider<ContextRefreshedEventFilter> contextRefreshedEventFilter) {
        return new TogglzApplicationContextBinderApplicationListener(contextRefreshedEventFilter.getIfAvailable());
    }

    @Configuration
    @ConditionalOnMissingBean(FeatureProvider.class)
    protected static class FeatureProviderConfiguration {

        @Autowired
        private TogglzProperties properties;

        @Bean
        public FeatureProvider featureProvider() {
            PropertyFeatureProvider provider = new PropertyFeatureProvider(properties.getFeatureProperties());
            Class<? extends Feature>[] featureEnums = properties.getFeatureEnums();
            if (featureEnums != null && featureEnums.length > 0) {
                return new CompositeFeatureProvider(new EnumBasedFeatureProvider(featureEnums), provider);
            } else {
                return provider;
            }
        }
    }

    @Configuration
    @ConditionalOnMissingBean(FeatureManager.class)
    protected static class FeatureManagerConfiguration {

        @Autowired
        private TogglzProperties properties;

        @Bean
        public FeatureManager featureManager(FeatureProvider featureProvider, List<StateRepository> stateRepositories,
                UserProvider userProvider, ActivationStrategyProvider activationStrategyProvider) {
            StateRepository stateRepository = null;
            if (stateRepositories.size() == 1) {
                stateRepository = stateRepositories.get(0);
            } else if (stateRepositories.size() > 1) {
                stateRepository = new CompositeStateRepository(
                        stateRepositories.toArray(new StateRepository[stateRepositories.size()]));
            }
            // If caching is enabled wrap state repository in caching state
            // repository.
            // Note that we explicitly check if the state repository is not
            // already a caching state repository,
            // as the auto configuration of the state repository already creates
            // a caching state repository if needed.
            // The below wrapping only occurs if the user provided the state
            // repository manually and caching is enabled.
            if (properties.getCache().isEnabled() && !(stateRepository instanceof CachingStateRepository)) {
                stateRepository = new CachingStateRepository(stateRepository, properties.getCache().getTimeToLive(),
                        properties.getCache().getTimeUnit());
            }
            FeatureManagerBuilder featureManagerBuilder = new FeatureManagerBuilder();
            String name = properties.getFeatureManagerName();
            if (name != null && name.length() > 0) {
                featureManagerBuilder.name(name);
            }
            featureManagerBuilder.featureProvider(featureProvider).stateRepository(stateRepository)
                    .userProvider(userProvider).activationStrategyProvider(activationStrategyProvider).build();
            FeatureManager manager = featureManagerBuilder.build();
            return manager;
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
            String featuresFile = properties.getFeaturesFile();
            if (featuresFile != null) {
                Resource resource = this.resourceLoader.getResource(featuresFile);
                Integer minCheckInterval = properties.getFeaturesFileMinCheckInterval();
                if (minCheckInterval != null) {
                    stateRepository = new FileBasedStateRepository(resource.getFile(), minCheckInterval);
                } else {
                    stateRepository = new FileBasedStateRepository(resource.getFile());
                }
            } else {
                Map<String, TogglzProperties.FeatureSpec> features = properties.getFeatures();
                stateRepository = new InMemoryStateRepository();
                for (String name : features.keySet()) {
                    stateRepository.setFeatureState(features.get(name).state(name));
                }
            }
            // If caching is enabled wrap state repository in caching state
            // repository.
            if (properties.getCache().isEnabled()) {
                stateRepository = new CachingStateRepository(stateRepository, properties.getCache().getTimeToLive(),
                        properties.getCache().getTimeUnit());
            }
            return stateRepository;
        }
    }

    @Configuration
    @ConditionalOnMissingClass("org.springframework.security.config.annotation.web.configuration.EnableWebSecurity")
    @ConditionalOnMissingBean(UserProvider.class)
    protected static class UserProviderConfiguration {
        @Bean
        public UserProvider userProvider() {
            return new NoOpUserProvider();
        }
    }

    @Configuration
    @ConditionalOnClass({ EnableWebSecurity.class, AuthenticationEntryPoint.class, SpringSecurityUserProvider.class })
    @ConditionalOnMissingBean(UserProvider.class)
    protected static class SpringSecurityUserProviderConfiguration {

        @Autowired
        private TogglzProperties properties;

        @Bean
        public UserProvider userProvider() {
            return new SpringSecurityUserProvider(properties.getConsole().getFeatureAdminAuthority());
        }
    }
}
