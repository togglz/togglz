package org.togglz.spring.boot.autoconfigure;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.togglz.console.TogglzConsoleServlet;

@ConditionalOnWebApplication
@ConditionalOnClass(TogglzConsoleServlet.class)
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
        return new ServletRegistrationBean(servlet, urlMapping);
    }

    protected String getContextPath() {
        return "";
    }

    static class OnConsoleEnabled extends AllNestedConditions {

        OnConsoleEnabled() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnProperty(prefix = "togglz.console", name = "enabled", matchIfMissing = true)
        static class OnConsole {
        }
    }

    public static class OnConsoleAndUseManagementPort extends OnConsoleEnabled {

        @ConditionalOnProperty(prefix = "togglz.console", name = "use-management-port", havingValue = "true", matchIfMissing = true)
        static class OnUseManagementPort {
        }

    }

    public static class OnConsoleAndNotUseManagementPort extends OnConsoleEnabled {

        @ConditionalOnProperty(prefix = "togglz.console", name = "use-management-port", havingValue = "false")
        static class OnNotUseManagementPort {
        }

    }
}
