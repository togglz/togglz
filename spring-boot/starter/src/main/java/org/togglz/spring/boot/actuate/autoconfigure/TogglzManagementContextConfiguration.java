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
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.togglz.console.TogglzConsoleServlet;

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
    @ConditionalOnClass(TogglzConsoleServlet.class)
    @ConditionalOnBean(ManagementServerProperties.class)
    @Conditional(TogglzConsoleBaseConfiguration.OnConsoleAndUseManagementPort.class)
    protected static class TogglzConsoleConfiguration extends TogglzConsoleBaseConfiguration {

        private final ManagementServerProperties managementServerProperties;

        protected TogglzConsoleConfiguration(TogglzProperties properties, ManagementServerProperties managementServerProperties) {
            super(properties);
            this.managementServerProperties = managementServerProperties;
        }

        @Override
        protected String getContextPath() {
            return managementServerProperties.getBasePath();
        }
    }

}
