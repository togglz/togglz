package org.togglz.spring.boot.actuate.autoconfigure;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.togglz.core.Feature;
import org.togglz.core.annotation.*;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

@SpringBootTest(
        properties = {
                "debug=true",
                "spring.main.banner-mode=off",
                "togglz.console.enabled=true",
                "togglz.console.use-management-port=true",
                "togglz.console.path=/togglz-console",
                "management.server.base-path=/manage",
                "togglz.feature-enums=org.togglz.spring.boot.actuate.autoconfigure.SpringBootManagementConfigurationTest.TestFeatures"
        }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableConfigurationProperties(ManagementServerProperties.class)
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class})
public class SpringBootManagementConfigurationTest {


    protected enum TestFeatures implements Feature {
        @Owner("someguy")
        @InfoLink("http://togglz.org/")
        @FeatureGroup("group1")
        @Label("Feature one")
        FEATURE_ONE,

        @EnabledByDefault
        FEATURE_TWO
    }

    @Configuration
    public static class TestConfig {

        @Bean
        UserProvider allIsWell() {
            return () -> new SimpleFeatureUser("admin", true);
        }
    }


    @Autowired
    TestRestTemplate testRestTemplate;


    @Test
    void testIsConsoleActive()  {
        ResponseEntity<String> entity = testRestTemplate.getForEntity("/manage/togglz-console/index", String.class);
        Assertions.assertEquals(HttpStatus.OK, entity.getStatusCode());
        Assertions.assertEquals(MediaType.TEXT_HTML, entity.getHeaders().getContentType());
    }


}
