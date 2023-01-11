package org.togglz.spring.boot.actuate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.jmx.support.JmxUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;
import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.FeatureGroup;
import org.togglz.core.annotation.InfoLink;
import org.togglz.core.annotation.Label;
import org.togglz.core.annotation.Owner;

import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "management.endpoints.web.exposure.include=togglz",
                "management.endpoints.jmx.exposure.include=togglz",
                "spring.main.banner-mode=off",
                "spring.jmx.enabled=true",
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
        FEATURE_TWO
    }

    @Configuration
    public static class TestConfig {

    }

    @Autowired
    private MockMvc mockMvc;


    @Test
    //@WithMockUser(username = "username", roles={"ADMIN"})
    void testWebActuatorEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/togglz"))
                .andExpect(status().isOk())
                .andExpect(content().json(readJson("all-features-initial.json")));

        mockMvc.perform(get("/actuator/togglz/FEATURE_ONE"))
                .andExpect(status().isOk())
                .andExpect(content().json(readJson("feature-one-initial.json")));

        mockMvc.perform(post("/actuator/togglz/FEATURE_ONE")
                        .contentType("application/json")
                        .content("{ \"enabled\": true }"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/togglz/FEATURE_ONE"))
                .andExpect(status().isOk())
                .andExpect(content().json(readJson("feature-one-enabled.json")));

        mockMvc.perform(post("/actuator/togglz/FEATURE_ONE")
                        .contentType("application/json")
                        .content("{ \"strategy\": \"aStrategy\" }"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/togglz/FEATURE_ONE"))
                .andExpect(status().isOk())
                .andExpect(content().json(readJson("feature-one-new-strategy.json")));

        mockMvc.perform(post("/actuator/togglz/FEATURE_ONE")
                        .contentType("application/json")
                        .content("{ \"parameters\": \"param1 = 10, param2 = 20 30, param3 = 40\" }"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/togglz/FEATURE_ONE"))
                .andExpect(status().isOk())
                .andExpect(content().json(readJson("feature-one-strategy-params.json")));

        mockMvc.perform(get("/actuator/togglz/FEATURE_ABSENT"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/actuator/togglz/FEATURE_ABSENT")
                        .contentType("application/json")
                        .content("{ \"parameters\": \"param1 = 10, param2 = 20 30, param3 = 40\" }"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/actuator/togglz/FEATURE_ONE")
                        .contentType("application/json")
                        .content("{ \"parameters\": \"param1: 10\" }"))
                .andExpect(status().isBadRequest());
    }


    @Test
    @SuppressWarnings("unchecked")
    void testJMXActuatorEndpoint() throws Exception {
        MBeanServer beanServer = JmxUtils.locateMBeanServer();
        ObjectName name = new ObjectName("org.springframework.boot:type=Endpoint,name=Togglz");

        List<Map<String, Object>> allFeatures = (List<Map<String, Object>>) beanServer.invoke(name, "getAllFeatures", new Object[0], new String[0]);
        assertEquals(2, allFeatures.size());

        Map<String, Object> feature = (Map<String, Object>) beanServer.invoke(name, "getFeature", new Object[]{"FEATURE_ABSENT"}, new String[]{String.class.getName()});
        assertNull(feature);

        feature = (Map<String, Object>) beanServer.invoke(name, "getFeature", new Object[]{"FEATURE_TWO"}, new String[]{String.class.getName()});
        assertTrue((Boolean) feature.get("enabled"));

        feature = (Map<String, Object>) beanServer.invoke(name, "setFeatureState"
                , new Object[]{"FEATURE_TWO", Boolean.FALSE, null, null},
                new String[]{String.class.getName(), Boolean.class.getName(), String.class.getName(), String.class.getName()});
        assertFalse((Boolean) feature.get("enabled"));

        assertThrows(MBeanException.class, () -> beanServer.invoke(name, "setFeatureState"
                , new Object[]{"FEATURE_ABSENT", Boolean.FALSE, null, null},
                new String[]{String.class.getName(), Boolean.class.getName(), String.class.getName(), String.class.getName()}));

        feature = (Map<String, Object>) beanServer.invoke(name, "setFeatureState"
                , new Object[]{"FEATURE_TWO", null, null, "param1 = 10, param2 = 20 30, param3 = 40"},
                new String[]{String.class.getName(), Boolean.class.getName(), String.class.getName(), String.class.getName()});
        Map<String, String> params = (Map<String, String>) feature.get("params");
        assertEquals("10", params.get("param1"));
        assertEquals("20 30", params.get("param2"));
        assertEquals("40", params.get("param3"));

        assertThrows(MBeanException.class, () -> beanServer.invoke(name, "setFeatureState"
                , new Object[]{"FEATURE_TWO", null, null, "param1: 10, param2 = 20 30, param3 = 40"},
                new String[]{String.class.getName(), Boolean.class.getName(), String.class.getName(), String.class.getName()}));
    }

    private String readJson(String file) {
        try (InputStream is = this.getClass().getResourceAsStream(file)) {
            return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
