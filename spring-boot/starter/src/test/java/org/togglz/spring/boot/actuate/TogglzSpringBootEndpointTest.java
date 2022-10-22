package org.togglz.spring.boot.actuate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;
import org.togglz.core.Feature;
import org.togglz.core.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "management.endpoints.web.exposure.include=togglz",
                "togglz.feature-enums=org.togglz.spring.boot.actuate.TogglzSpringBootEndpointTest.TestFeatures"
        })
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
public class TogglzSpringBootEndpointTest {

    protected enum TestFeatures implements Feature {
        @Owner("someguy")
        @InfoLink("http://togglz.org/")
        @FeatureGroup("group1")
        @Label("Feature one")
        FEATURE_ONE,

        @EnabledByDefault
        FEATURE_TWO;
    }

    @Configuration
    public static class TestConfig {

    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testActuatorEndpoint() throws Exception {
        mockMvc
                .perform(get("/actuator/togglz"))
                .andExpect(status().isOk())
                .andExpect(content().json(readJson("all-features-initial.json")));
        mockMvc
                .perform(get("/actuator/togglz/FEATURE_ONE"))
                .andExpect(status().isOk())
                .andExpect(content().json(readJson("feature-one-initial.json")));
        mockMvc
                .perform(post("/actuator/togglz/FEATURE_ONE")
                        .contentType("application/json")
                        .content("{ \"enabled\": true }"))
                .andExpect(status().isOk());
        mockMvc
                .perform(get("/actuator/togglz/FEATURE_ONE"))
                .andExpect(status().isOk())
                .andExpect(content().json(readJson("feature-one-after.json")));
    }

    private String readJson(String file) {
        try (InputStream is = this.getClass().getResourceAsStream(file)) {
            return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
