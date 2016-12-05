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
import org.springframework.boot.actuate.autoconfigure.ManagementServerPropertiesAutoConfiguration;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.togglz.core.Feature;
import org.togglz.core.activation.Parameter;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.EmptyFeatureProvider;
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

import static org.junit.Assert.*;

/**
 * Tests for {@link TogglzAutoConfiguration}.
 *
 * @author Marcel Overdijk
 */
public class TogglzAutoConfigurationTest {

    private AnnotationConfigWebApplicationContext context;

    @After
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void defaultTogglz() {
        load(new Class[]{FeatureProviderConfig.class, TogglzAutoConfiguration.class, TogglzManagementContextConfiguration.class, ManagementServerPropertiesAutoConfiguration.class});
        FeatureManager featureManager = this.context.getBean(FeatureManager.class);
        Set<Feature> features = featureManager.getFeatures();

        assertNotNull(featureManager);
        assertSame(featureManager, FeatureContext.getFeatureManager());

        assertSame(this.context, ContextClassLoaderApplicationContextHolder.get());

        assertEquals(2, features.size());
        assertTrue(features.contains(MyFeatures.FEATURE_ONE));
        assertTrue(features.contains(MyFeatures.FEATURE_TWO));

        assertTrue(this.context.getBean(StateRepository.class) instanceof InMemoryStateRepository);
        assertEquals(1, this.context.getBeansOfType(ServletRegistrationBean.class).size());
        assertTrue(this.context.getBean(ServletRegistrationBean.class).getUrlMappings().contains("/togglz-console/*"));

        TogglzEndpoint togglzEndpoint = this.context.getBean(TogglzEndpoint.class);
        assertEquals("togglz", togglzEndpoint.getId());
        assertTrue(togglzEndpoint.isEnabled());
        assertTrue(togglzEndpoint.isSensitive());
    }

    @Test
    public void applicationContextBinder() {
        load(new Class[]{FeatureProviderConfig.class, TogglzAutoConfiguration.class, TogglzManagementContextConfiguration.class, ManagementServerPropertiesAutoConfiguration.class});
        assertSame(this.context, ContextClassLoaderApplicationContextHolder.get());
    }

    @Test
    public void disabled() {
        // Explicitly clear cache
        FeatureContext.clearCache();

        load(new Class[]{FeatureProviderConfig.class, TogglzAutoConfiguration.class, TogglzManagementContextConfiguration.class, ManagementServerPropertiesAutoConfiguration.class},
                "togglz.enabled: false");

        assertNull(FeatureContext.getFeatureManagerOrNull());
        assertNull(ContextClassLoaderApplicationContextHolder.get());

        assertEquals(0, this.context.getBeansOfType(FeatureManager.class).size());
        assertEquals(0, this.context.getBeansOfType(ActivationStrategy.class).size());
        assertEquals(0, this.context.getBeansOfType(StateRepository.class).size());
        assertEquals(0, this.context.getBeansOfType(UserProvider.class).size());
        assertEquals(0, this.context.getBeansOfType(ServletRegistrationBean.class).size());
        assertEquals(0, this.context.getBeansOfType(TogglzEndpoint.class).size());
    }

    @Test
    public void noFeatureProviderBeanAndFeatureEnumsProperty() {
        load(new Class[]{TogglzAutoConfiguration.class, TogglzManagementContextConfiguration.class, ManagementServerPropertiesAutoConfiguration.class});
        assertTrue(this.context.getBean(FeatureProvider.class) instanceof EmptyFeatureProvider);
    }

    @Test
    public void featureEnums() {
        load(new Class[]{TogglzAutoConfiguration.class, TogglzManagementContextConfiguration.class, ManagementServerPropertiesAutoConfiguration.class},
                "togglz.feature-enums: org.togglz.spring.boot.autoconfigure.TogglzAutoConfigurationTest.MyFeatures");
        FeatureManager featureManager = this.context.getBean(FeatureManager.class);
        Set<Feature> features = featureManager.getFeatures();
        assertNotNull(featureManager);
        assertEquals(2, features.size());
        assertTrue(features.contains(MyFeatures.FEATURE_ONE));
        assertTrue(features.contains(MyFeatures.FEATURE_TWO));
    }

    @Test(expected = BeanCreationException.class)
    public void featureEnumsClassNotFound() {
        load(new Class[]{TogglzAutoConfiguration.class, TogglzManagementContextConfiguration.class, ManagementServerPropertiesAutoConfiguration.class},
                "togglz.feature-enums: i.dont.exist.features");
    }

    @Test
    public void customFeatureManagerName() {
        load(new Class[]{FeatureProviderConfig.class, TogglzAutoConfiguration.class, TogglzManagementContextConfiguration.class, ManagementServerPropertiesAutoConfiguration.class},
                "togglz.feature-manager-name: Custom Feature Manager Name");
        assertEquals("Custom Feature Manager Name", this.context.getBean(FeatureManager.class).getName());
    }

    @Test
    public void features() {
        load(new Class[]{FeatureProviderConfig.class, TogglzAutoConfiguration.class, TogglzManagementContextConfiguration.class, ManagementServerPropertiesAutoConfiguration.class},
                "togglz.features.FEATURE_ONE: true",
                "togglz.features.FEATURE_TWO: false");
        FeatureManager featureManager = this.context.getBean(FeatureManager.class);
        assertTrue(featureManager.isActive(MyFeatures.FEATURE_ONE));
        assertFalse(featureManager.isActive(MyFeatures.FEATURE_TWO));
        assertTrue(this.context.getBean(StateRepository.class) instanceof PropertyBasedStateRepository);
    }

    @Test
    public void featuresFile() {
        load(new Class[]{FeatureProviderConfig.class, TogglzAutoConfiguration.class, TogglzManagementContextConfiguration.class, ManagementServerPropertiesAutoConfiguration.class},
                "togglz.features-file: classpath:/features-file/features.properties");
        FeatureManager featureManager = this.context.getBean(FeatureManager.class);
        assertTrue(featureManager.isActive(MyFeatures.FEATURE_ONE));
        assertFalse(featureManager.isActive(MyFeatures.FEATURE_TWO));
        assertTrue(this.context.getBean(StateRepository.class) instanceof FileBasedStateRepository);
    }

    @Test
    public void cacheEnabled() {
        load(new Class[]{FeatureProviderConfig.class, TogglzAutoConfiguration.class, TogglzManagementContextConfiguration.class, ManagementServerPropertiesAutoConfiguration.class},
                "togglz.cache.enabled: true");
        assertTrue(this.context.getBean(StateRepository.class) instanceof CachingStateRepository);
    }

    @Test
    public void consoleDisabled() {
        load(new Class[]{FeatureProviderConfig.class, TogglzAutoConfiguration.class, TogglzManagementContextConfiguration.class, ManagementServerPropertiesAutoConfiguration.class},
                "togglz.console.enabled: false");
        assertEquals(0, this.context.getBeansOfType(ServletRegistrationBean.class).size());
    }

    @Test
    public void consoleWithCustomManagementContextPath() {
        // With TogglzManagementContextConfiguration responsible for creating the admin console servlet registration bean,
        // if a custom managememnt context path is provided it should be used as prefix.
        load(new Class[]{FeatureProviderConfig.class, TogglzAutoConfiguration.class, TogglzManagementContextConfiguration.class, ManagementServerPropertiesAutoConfiguration.class},
                "management.context-path: /manage");
        assertEquals(1, this.context.getBeansOfType(ServletRegistrationBean.class).size());
        assertTrue(this.context.getBean(ServletRegistrationBean.class).getUrlMappings().contains("/manage/togglz-console/*"));
    }

    @Test
    public void consoleUseManagementPortIsFalseWithoutTogglzManagementContextConfiguration() {
        // With togglz.console.use-management-port: false the TogglzAutoConfiguration is responsible for creating the admin console servlet
        // registration bean.
        // We explicitly do not load the TogglzManagementContextConfiguration to test the registration bean is created by the
        // TogglzAutoConfiguration, hence asserting on 1 bean.
        load(new Class[]{FeatureProviderConfig.class, TogglzAutoConfiguration.class, ManagementServerPropertiesAutoConfiguration.class},
                "togglz.console.use-management-port: false");
        assertEquals(1, this.context.getBeansOfType(ServletRegistrationBean.class).size());
        assertTrue(this.context.getBean(ServletRegistrationBean.class).getUrlMappings().contains("/togglz-console/*"));
    }

    @Test
    public void consoleUseManagementPortIsTrueWithoutTogglzManagementContextConfiguration() {
        // With togglz.console.use-management-port: true the TogglzManagementContextConfiguration is responsible for creating the admin
        // console servlet registration bean.
        // We explicitly do not load the TogglzManagementContextConfiguration to test the registration bean is not added to the context,
        // hence asserting on 0 beans.
        load(new Class[]{FeatureProviderConfig.class, TogglzAutoConfiguration.class, ManagementServerPropertiesAutoConfiguration.class},
                "togglz.console.use-management-port: true");
        assertEquals(0, this.context.getBeansOfType(ServletRegistrationBean.class).size());
    }

    @Test
    public void customConsolePath() {
        load(new Class[]{FeatureProviderConfig.class, TogglzAutoConfiguration.class, TogglzManagementContextConfiguration.class, ManagementServerPropertiesAutoConfiguration.class},
                "togglz.console.path: /custom");
        assertEquals(1, this.context.getBeansOfType(ServletRegistrationBean.class).size());
        assertTrue(this.context.getBean(ServletRegistrationBean.class).getUrlMappings().contains("/custom/*"));
    }

    @Test
    public void customConsolePathWithTrailingSlash() {
        load(new Class[]{FeatureProviderConfig.class, TogglzAutoConfiguration.class, TogglzManagementContextConfiguration.class, ManagementServerPropertiesAutoConfiguration.class},
                "togglz.console.path: /custom/");
        assertEquals(1, this.context.getBeansOfType(ServletRegistrationBean.class).size());
        assertTrue(this.context.getBean(ServletRegistrationBean.class).getUrlMappings().contains("/custom/*"));
    }

    @Test
    public void endpointDisabled() {
        load(new Class[]{FeatureProviderConfig.class, TogglzAutoConfiguration.class, TogglzManagementContextConfiguration.class, ManagementServerPropertiesAutoConfiguration.class},
                "togglz.endpoint.enabled: false");
        assertEquals(0, this.context.getBeansOfType(TogglzEndpoint.class).size());
    }

    @Test
    public void endpointNotSensitive() {
        load(new Class[]{FeatureProviderConfig.class, TogglzAutoConfiguration.class, TogglzManagementContextConfiguration.class, ManagementServerPropertiesAutoConfiguration.class},
                "togglz.endpoint.sensitive: false");
        assertFalse(this.context.getBean(TogglzEndpoint.class).isSensitive());
    }

    @Test
    public void customEndpointId() {
        load(new Class[]{FeatureProviderConfig.class, TogglzAutoConfiguration.class, TogglzManagementContextConfiguration.class, ManagementServerPropertiesAutoConfiguration.class},
                "togglz.endpoint.id: features");
        assertEquals("features", this.context.getBean(TogglzEndpoint.class).getId());
    }

    @Test
    public void customActivationStrategy() {
        load(new Class[]{FeatureProviderConfig.class, ActivationStrategyConfig.class, TogglzAutoConfiguration.class, TogglzManagementContextConfiguration.class, ManagementServerPropertiesAutoConfiguration.class});
        FeatureManager featureManager = this.context.getBean(FeatureManager.class);
        CustomActivationStrategy customActivationStrategy = this.context.getBean(CustomActivationStrategy.class);
        assertTrue(featureManager.getActivationStrategies().contains(customActivationStrategy));
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
