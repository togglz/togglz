package org.togglz.spring.boot2.autoconfigure.actuator;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.manager.FeatureManager;
import org.togglz.spring.boot.autoconfigure.TogglzAutoConfiguration;

@Configuration
@ConditionalOnClass(Endpoint.class)
@AutoConfigureAfter(TogglzAutoConfiguration.class)
public class TogglzEndpointAutoConfiguration {

    @Bean
    @ConditionalOnBean(FeatureManager.class)
    @ConditionalOnMissingBean
    @ConditionalOnEnabledEndpoint
    public TogglzEndpoint togglzEndpoint(FeatureManager featureManager) {
        return new TogglzEndpoint(featureManager);
    }
}
