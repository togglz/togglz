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

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.togglz.core.Feature;
import org.togglz.core.activation.Parameter;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.cache.CachingStateRepository;
import org.togglz.core.repository.file.FileBasedStateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.repository.property.PropertyBasedStateRepository;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.UserProvider;
import org.togglz.spring.util.ContextClassLoaderApplicationContextHolder;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for {@link TogglzAutoConfiguration}.
 *
 * @author Marcel Overdijk
 */
public class TogglzAutoConfigurationTests {

    private AnnotationConfigWebApplicationContext context;

    @After
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void defaultTogglz() {
        load(new Class[]{TogglzAutoConfiguration.class, FeatureProviderConfig.class});
        FeatureManager featureManager = this.context.getBean(FeatureManager.class);
        Set<Feature> features = featureManager.getFeatures();
        assertThat(featureManager, is(notNullValue()));
        assertThat(features, hasSize(2));
        assertThat(features, hasItem(MyFeatures.FEATURE_ONE));
        assertThat(features, hasItem(MyFeatures.FEATURE_TWO));
        assertThat(this.context.getBean(StateRepository.class), is(instanceOf(InMemoryStateRepository.class)));
        assertThat(this.context.getBeansOfType(ServletRegistrationBean.class).size(), is(equalTo(1)));
        assertThat(this.context.getBean(ServletRegistrationBean.class).getUrlMappings(), hasItem("/togglz-console/*"));
        TogglzEndpoint togglzEndpoint = this.context.getBean(TogglzEndpoint.class);
        assertThat(togglzEndpoint.getId(), is("togglz"));
        assertThat(togglzEndpoint.isEnabled(), is(true));
        assertThat(togglzEndpoint.isSensitive(), is(true));
        assertThat(ContextClassLoaderApplicationContextHolder.get(), is((ApplicationContext) this.context));
        assertThat(FeatureContext.getFeatureManager(), is(sameInstance(featureManager)));
    }

    @Test
    public void applicationContextBinder() {
        load(new Class[]{TogglzAutoConfiguration.class, FeatureProviderConfig.class});
        assertThat(ContextClassLoaderApplicationContextHolder.get(), is((ApplicationContext) this.context));
    }

    @Test
    public void disabled() {
        load(new Class[]{TogglzAutoConfiguration.class, FeatureProviderConfig.class},
                "togglz.enabled: false");
        assertThat(this.context.getBeansOfType(FeatureManager.class).size(), is(0));
        assertThat(this.context.getBeansOfType(ActivationStrategy.class).size(), is(0));
        assertThat(this.context.getBeansOfType(StateRepository.class).size(), is(0));
        assertThat(this.context.getBeansOfType(UserProvider.class).size(), is(0));
        assertThat(this.context.getBeansOfType(ServletRegistrationBean.class).size(), is(0));
        assertThat(this.context.getBeansOfType(TogglzEndpoint.class).size(), is(0));
        assertThat(ContextClassLoaderApplicationContextHolder.get(), is(nullValue()));
        try {
            assertThat(FeatureContext.getFeatureManager(), is(nullValue()));
            fail();
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void featureEnums() {
        load(new Class[]{TogglzAutoConfiguration.class},
                "togglz.feature-enums: org.togglz.spring.boot.autoconfigure.TogglzAutoConfigurationTests.MyFeatures");
        FeatureManager featureManager = this.context.getBean(FeatureManager.class);
        Set<Feature> features = featureManager.getFeatures();
        assertThat(featureManager, is(notNullValue()));
        assertThat(features, hasSize(2));
        assertThat(features, hasItem(MyFeatures.FEATURE_ONE));
        assertThat(features, hasItem(MyFeatures.FEATURE_TWO));
    }

    @Test(expected = BeanCreationException.class)
    public void featureEnumsClassNotFound() {
        load(new Class[]{TogglzAutoConfiguration.class},
                "togglz.feature-enums: i.dont.exist.features");
    }

    @Test
    public void customFeatureManagerName() {
        load(new Class[]{TogglzAutoConfiguration.class, FeatureProviderConfig.class},
                "togglz.feature-manager-name: Custom Feature Manager Name");
        assertThat(this.context.getBean(FeatureManager.class).getName(), is("Custom Feature Manager Name"));
    }

    @Test
    public void features() {
        load(new Class[]{TogglzAutoConfiguration.class, FeatureProviderConfig.class},
                "togglz.features.FEATURE_ONE: true",
                "togglz.features.FEATURE_TWO: false");
        FeatureManager featureManager = this.context.getBean(FeatureManager.class);
        assertThat(featureManager.isActive(MyFeatures.FEATURE_ONE), is(true));
        assertThat(featureManager.isActive(MyFeatures.FEATURE_TWO), is(false));
        assertThat(this.context.getBean(StateRepository.class), is(instanceOf(PropertyBasedStateRepository.class)));
    }

    @Test
    public void featuresFile() {
        load(new Class[]{TogglzAutoConfiguration.class, FeatureProviderConfig.class},
                "togglz.features-file: classpath:/features-file/features.properties");
        FeatureManager featureManager = this.context.getBean(FeatureManager.class);
        assertThat(featureManager.isActive(MyFeatures.FEATURE_ONE), is(true));
        assertThat(featureManager.isActive(MyFeatures.FEATURE_TWO), is(false));
        assertThat(this.context.getBean(StateRepository.class), is(instanceOf(FileBasedStateRepository.class)));
    }

    @Test
    public void cacheEnabled() {
        load(new Class[]{TogglzAutoConfiguration.class, FeatureProviderConfig.class},
                "togglz.cache.enabled: true");
        assertThat(this.context.getBean(StateRepository.class), is(instanceOf(CachingStateRepository.class)));
    }

    @Test
    public void consoleDisabled() {
        load(new Class[]{TogglzAutoConfiguration.class, FeatureProviderConfig.class},
                "togglz.console.enabled: false");
        assertThat(this.context.getBeansOfType(ServletRegistrationBean.class).size(), is(0));
    }

    @Test
    public void customConsolePath() {
        load(new Class[]{TogglzAutoConfiguration.class, FeatureProviderConfig.class},
                "togglz.console.path: /custom");
        assertThat(this.context.getBeansOfType(ServletRegistrationBean.class).size(), is(1));
        assertThat(this.context.getBean(ServletRegistrationBean.class).getUrlMappings(), hasItem("/custom/*"));
    }

    @Test
    public void customConsolePathWithTrailingSlash() {
        load(new Class[]{TogglzAutoConfiguration.class, FeatureProviderConfig.class},
                "togglz.console.path: /custom/");
        assertThat(this.context.getBeansOfType(ServletRegistrationBean.class).size(), is(1));
        assertThat(this.context.getBean(ServletRegistrationBean.class).getUrlMappings(), hasItems("/custom/*"));
    }

    @Test
    public void endpointDisabled() {
        load(new Class[]{TogglzAutoConfiguration.class, FeatureProviderConfig.class},
                "togglz.endpoint.enabled: false");
        assertThat(this.context.getBeansOfType(TogglzEndpoint.class).size(), is(0));
    }

    @Test
    public void endpointNotSensitive() {
        load(new Class[]{TogglzAutoConfiguration.class, FeatureProviderConfig.class},
                "togglz.endpoint.sensitive: false");
        assertThat(this.context.getBean(TogglzEndpoint.class).isSensitive(), is(false));
    }

    @Test
    public void customEndpointId() {
        load(new Class[]{TogglzAutoConfiguration.class, FeatureProviderConfig.class},
                "togglz.endpoint.id: features");
        assertThat(this.context.getBean(TogglzEndpoint.class).getId(), is("features"));
    }

    @Test
    public void customActivationStrategy() {
        load(new Class[]{TogglzAutoConfiguration.class, FeatureProviderConfig.class, ActivationStrategyConfig.class});
        FeatureManager featureManager = this.context.getBean(FeatureManager.class);
        CustomActivationStrategy customActivationStrategy = this.context.getBean(CustomActivationStrategy.class);
        assertThat(featureManager.getActivationStrategies().contains(customActivationStrategy), is(true));
    }

    private void load(Class<?>[] configs, String... environment) {
        this.context = new AnnotationConfigWebApplicationContext();
        this.context.register(configs);
        EnvironmentTestUtils.addEnvironment(this.context, environment);
        this.context.refresh();
    }

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
    protected static class FeatureProviderConfig {

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
}
