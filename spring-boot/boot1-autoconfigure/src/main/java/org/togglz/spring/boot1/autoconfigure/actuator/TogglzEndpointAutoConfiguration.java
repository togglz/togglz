package org.togglz.spring.boot1.autoconfigure.actuator;

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
