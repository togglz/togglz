package org.togglz.spring.boot1.autoconfigure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.springframework.boot.actuate.autoconfigure.ManagementServerPropertiesAutoConfiguration;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.togglz.core.Feature;
import org.togglz.core.activation.Parameter;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.FeatureUser;
import org.togglz.spring.boot.autoconfigure.TogglzAutoConfiguration;
import org.togglz.spring.boot1.autoconfigure.actuator.TogglzEndpointAutoConfiguration;
import org.togglz.spring.boot1.autoconfigure.management.TogglzManagementContextConfiguration;

public class BaseTest {

    protected AnnotationConfigWebApplicationContext context;

    @After
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    protected void loadWithDefaults(String... environment) {
        loadWithDefaults(new Class[]{}, environment);
    }

    protected void loadWithDefaults(Class<?>[] configs, String... environment) {
        List<Class<?>> mergedConfigs = new ArrayList<>();
        mergedConfigs.add(ManagementServerPropertiesAutoConfiguration.class);
        mergedConfigs.add(TogglzAutoConfiguration.class);
        mergedConfigs.add(TogglzEndpointAutoConfiguration.class);
        mergedConfigs.add(TogglzManagementContextConfiguration.class);
        mergedConfigs.addAll(Arrays.asList(configs));
        load(mergedConfigs.toArray(new Class[mergedConfigs.size()]), environment);
    }

    protected void load(Class<?>[] configs, String... environment) {
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

}