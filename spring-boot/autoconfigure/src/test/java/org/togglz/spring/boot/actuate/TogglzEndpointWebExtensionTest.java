package org.togglz.spring.boot.actuate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.http.HttpStatus;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzAutoConfiguration;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzEndpointAutoConfiguration;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzFeature;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TogglzEndpointWebExtensionTest extends BaseTest {

    @Test
    public void shouldEnableAFeature() {
        contextRunner.withConfiguration(AutoConfigurations.of(
                        DispatcherServletPathConfig.class,
                        TogglzAutoConfiguration.class,
                        TogglzEndpointAutoConfiguration.class))
                .withPropertyValues(
                        "management.endpoints.web.exposure.include=*",
                        "togglz.features.FEATURE_ONE.enabled: false",
                        "togglz.features.FEATURE_ONE.label: Feature One",
                        "togglz.features.FEATURE_ONE.groups: GROUP1,GROUP2")
                .run((context) -> {
                    // Given
                    TogglzEndpointWebExtension endpoint = context.getBean(TogglzEndpointWebExtension.class);

                    // When
                    final WebEndpointResponse<TogglzFeature> response = endpoint.setFeatureState(
                            "FEATURE_ONE", true, null, null);

                    // Then
                    assertEquals(HttpStatus.OK.value(), response.getStatus());
                    TogglzFeature togglzFeature = response.getBody();
                    assertTrue(togglzFeature.isEnabled());
                    Set<String> expected = new HashSet<>();
                    expected.add("GROUP1");
                    expected.add("GROUP2");
                    assertEquals(expected, togglzFeature.getMetadata().getGroups());
                    assertFalse(togglzFeature.getMetadata().isEnabledByDefault());
                });
    }

    @Test
    public void shouldChangeStrategy() {
        contextRunner.withConfiguration(AutoConfigurations.of(
                        DispatcherServletPathConfig.class,
                        TogglzAutoConfiguration.class,
                        TogglzEndpointAutoConfiguration.class))
                .withPropertyValues(
                        "management.endpoints.web.exposure.include=*",
                        "togglz.features.FEATURE_ONE.enabled: false")
                .run((context) -> {
                    // Given
                    TogglzEndpointWebExtension endpoint = context.getBean(TogglzEndpointWebExtension.class);
                    String newStrategy = "aStrategy";

                    // When
                    final WebEndpointResponse<TogglzFeature> response = endpoint.setFeatureState(
                            "FEATURE_ONE", null, newStrategy, null);

                    // Then
                    assertEquals(HttpStatus.OK.value(), response.getStatus());
                    TogglzFeature togglzFeature = response.getBody();
                    assertEquals(newStrategy, togglzFeature.getStrategy());
                });
    }

    @Test
    public void shouldChangeStrategyParameters() {
        contextRunner.withConfiguration(AutoConfigurations.of(
                        DispatcherServletPathConfig.class,
                        TogglzAutoConfiguration.class,
                        TogglzEndpointAutoConfiguration.class))
                .withPropertyValues(
                        "management.endpoints.web.exposure.include=*",
                        "togglz.features.FEATURE_ONE.enabled: false",
                        "togglz.features.FEATURE_ONE.parameters: {param1: 0, param2: 100}")
                .run((context) -> {
                    // Given
                    TogglzEndpointWebExtension endpoint = context.getBean(TogglzEndpointWebExtension.class);
                    String parametersString = "param1 = 10, param2 = 20 30, param3 = 40";

                    // When
                    final WebEndpointResponse<TogglzFeature> response = endpoint.setFeatureState(
                            "FEATURE_ONE", null, null, parametersString);

                    // Then
                    assertEquals(HttpStatus.OK.value(), response.getStatus());
                    TogglzFeature togglzFeature = response.getBody();
                    Map<String, String> params = togglzFeature.getParams();
                    assertEquals("10", params.get("param1"));
                    assertEquals("20 30", params.get("param2"));
                    assertEquals("40", params.get("param3"));
                });
    }

    @Test
    public void shouldDisableAFeature() {
        contextRunner.withConfiguration(AutoConfigurations.of(
                        DispatcherServletPathConfig.class,
                        TogglzAutoConfiguration.class,
                        TogglzEndpointAutoConfiguration.class))
                .withPropertyValues(
                        "management.endpoints.web.exposure.include=*",
                        "togglz.features.FEATURE_ONE.enabled: true")
                .run((context) -> {
                    // Given
                    TogglzEndpointWebExtension endpoint = context.getBean(TogglzEndpointWebExtension.class);

                    // When
                    final WebEndpointResponse<TogglzFeature> response = endpoint.setFeatureState(
                            "FEATURE_ONE", false, null, null);

                    // Then
                    assertEquals(HttpStatus.OK.value(), response.getStatus());
                    TogglzFeature togglzFeature = response.getBody();
                    assertFalse(togglzFeature.isEnabled());
                });
    }

    @Test
    public void should404IfTheFeatureDoesNotExist() {
        contextRunner.withConfiguration(AutoConfigurations.of(
                        DispatcherServletPathConfig.class,
                        TogglzAutoConfiguration.class,
                        TogglzEndpointAutoConfiguration.class))
                .withPropertyValues("management.endpoints.web.exposure.include=*")
                .run((context) -> {
                    // Given
                    TogglzEndpointWebExtension endpoint = context.getBean(TogglzEndpointWebExtension.class);

                    // When
                    final WebEndpointResponse<TogglzFeature> response = endpoint.setFeatureState(
                            "FEATURE_ONE", false, null, null);
                    assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
                });
    }

    @Test
    public void should400IfFormatOfParameterIsIncorrect() {
        contextRunner.withConfiguration(AutoConfigurations.of(
                        DispatcherServletPathConfig.class,
                        TogglzAutoConfiguration.class,
                        TogglzEndpointAutoConfiguration.class))
                .withPropertyValues(
                        "management.endpoints.web.exposure.include=*",
                        "togglz.features.FEATURE_ONE.strategy: strategy",
                        "togglz.features.FEATURE_ONE.parameters: {param1: 0, param2: 100}")
                .run((context) -> {
                    // Given
                    TogglzEndpointWebExtension endpoint = context.getBean(TogglzEndpointWebExtension.class);
                    String parametersString = "param1: 10";

                    // When
                    final WebEndpointResponse<TogglzFeature> response = endpoint.setFeatureState(
                            "FEATURE_ONE", false, null, parametersString);
                    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

                });
    }

}
