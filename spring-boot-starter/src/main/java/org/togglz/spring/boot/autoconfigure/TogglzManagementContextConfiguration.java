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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.togglz.console.TogglzConsoleServlet;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Togglz.
 *
 * @author Marcel Overdijk
 */
@ManagementContextConfiguration
@ConditionalOnBean(ManagementServerProperties.class)
@ConditionalOnProperty(prefix = "togglz", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties({TogglzProperties.class})
public class TogglzManagementContextConfiguration {

    @Configuration
    @ConditionalOnWebApplication
    @ConditionalOnClass(TogglzConsoleServlet.class)
    @Conditional(OnConsoleAndUseManagementPort.class)
    protected static class TogglzConsoleConfiguration {

        @Autowired
        private TogglzProperties properties;

        @Autowired
        private ManagementServerProperties managementServerProperties;

        @Bean
        public ServletRegistrationBean togglzConsole() {
            String path = managementServerProperties.getContextPath() + properties.getConsole().getPath();
            String urlMapping = (path.endsWith("/") ? path + "*" : path + "/*");
            TogglzConsoleServlet servlet = new TogglzConsoleServlet();
            servlet.setSecured(properties.getConsole().isSecured());
            return new ServletRegistrationBean(servlet, urlMapping);
        }
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
