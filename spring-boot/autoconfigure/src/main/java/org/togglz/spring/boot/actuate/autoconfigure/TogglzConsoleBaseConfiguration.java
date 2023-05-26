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

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.togglz.console.TogglzConsoleServlet;

/**
 * Base {@link EnableAutoConfiguration Auto-configuration} class for Togglz Console.
 *
 * <p>Provides a common ground implementation for console on management port or on the
 * application port, as well as for Spring Boot 1.5 and Spring Boot 2.
 *
 * @author Marcel Overdijk
 * @author Rui Figueira
 */
public abstract class TogglzConsoleBaseConfiguration {

    private final TogglzProperties properties;

    protected TogglzConsoleBaseConfiguration(TogglzProperties properties) {
        this.properties = properties;
    }

    @Bean
    public ServletRegistrationBean togglzConsole() {
        String path = getContextPath() + properties.getConsole().getPath();
        String urlMapping = (path.endsWith("/") ? path + "*" : path + "/*");
        TogglzConsoleServlet servlet = new TogglzConsoleServlet();
        servlet.setSecured(properties.getConsole().isSecured());
        servlet.setValidateCSRFToken(properties.getConsole().isValidateCSRFToken());
        return new ServletRegistrationBean(servlet, urlMapping);
    }

    protected String getContextPath() {
        return "";
    }

    public static class OnConsoleAndUseManagementPort extends AllNestedConditions {

        OnConsoleAndUseManagementPort() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnProperty(prefix = "togglz.console", name = "enabled", matchIfMissing = true)
        static class OnConsole {
        }
        
        @ConditionalOnProperty(prefix = "togglz.console", name = "use-management-port", havingValue = "true", matchIfMissing = true)
        static class OnUseManagementPort {
        }

    }

    public static class OnConsoleAndNotUseManagementPort extends AllNestedConditions {

        OnConsoleAndNotUseManagementPort() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnProperty(prefix = "togglz.console", name = "enabled", matchIfMissing = true)
        static class OnConsole {
        }

        @ConditionalOnProperty(prefix = "togglz.console", name = "use-management-port", havingValue = "false", matchIfMissing = true)
        static class OnNotUseManagementPort {
        }

    }
}
