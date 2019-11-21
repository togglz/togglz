package org.togglz.spring.boot.actuate.autoconfigure;

import org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.togglz.spring.boot.autoconfigure.TogglzAutoConfiguration;
import org.togglz.spring.boot.autoconfigure.TogglzConsoleBaseConfiguration;
import org.togglz.spring.boot.autoconfigure.TogglzProperties;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Togglz.
 *
 * @author Marcel Overdijk
 */
@ManagementContextConfiguration
@ConditionalOnClass(Endpoint.class)
@ConditionalOnProperty(prefix = "togglz", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties({TogglzProperties.class})
@AutoConfigureAfter({ TogglzAutoConfiguration.class, ManagementContextAutoConfiguration.class })
public class TogglzManagementContextConfiguration {

    @Configuration
    @ConditionalOnWebApplication
    @ConditionalOnBean(ManagementServerProperties.class)
    protected static class TogglzConsoleConfiguration {

        private final ManagementServerProperties managementServerProperties;

        protected TogglzConsoleConfiguration(TogglzProperties properties, ManagementServerProperties managementServerProperties) {
            this.managementServerProperties = managementServerProperties;
        }

        protected String getContextPath() {
            return managementServerProperties.getServlet().getContextPath();
        }
    }
}