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

package org.togglz.spring.boot.actuate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.togglz.core.Feature;
import org.togglz.core.annotation.*;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzAutoConfiguration;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzEndpointAutoConfiguration;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzFeature;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Spring Boot 2 compatible {@link TogglzEndpoint}.
 *
 * @author Marcel Overdijk
 * @author Rui Figueira
 */
public class TogglzEndpointTest extends BaseTest {

    public enum TestFeatures implements Feature {

        @FeatureGroup("GROUP1")
        @Label("Feature one")
        @InfoLink("http://togglz.org/")
        FEATURE_ONE,

        @FeatureGroup("GROUP2")
        @Owner("someguy")
        @Label("Feature two")
        @EnabledByDefault
        FEATURE_TWO,

        FEATURE_THREE

    }

    @Test
    public void getAllFeatures() {
        contextRunner.withConfiguration(AutoConfigurations.of(
                DispatcherServletPathConfig.class,
                TogglzAutoConfiguration.class,
                TogglzEndpointAutoConfiguration.class))
            .withPropertyValues(
                    "management.endpoints.web.exposure.include=*",
                    "togglz.features.FEATURE_ONE.enabled: true",
                    "togglz.features.FEATURE_TWO.enabled: false",
                    "togglz.features.FEATURE_TWO.label: Feature Two",
                    "togglz.features.FEATURE_TWO.groups: GROUP1,GROUP2",
                    "togglz.features.FEATURE_TWO.strategy: release-date",
                    "togglz.features.FEATURE_TWO.param.date: 2016-07-01",
                    "togglz.features.FEATURE_TWO.param.time: 08:30:00")
            .run((context) -> {
                TogglzEndpoint endpoint = context.getBean(TogglzEndpoint.class);
                List<TogglzFeature> features = endpoint.getAllFeatures();

                // Assert we have 2 features
                assertEquals(2, features.size());

                // Assert feature one
                assertEquals("FEATURE_ONE", features.get(0).getName());
                assertTrue(features.get(0).isEnabled());
                assertNull(features.get(0).getStrategy());
                assertEquals(0, features.get(0).getParams().size());
                assertEquals(Collections.emptyMap(), features.get(0).getParams());

                // Assert feature two
                assertEquals("FEATURE_TWO", features.get(1).getName());
                assertFalse(features.get(1).isEnabled());
                assertEquals("release-date", features.get(1).getStrategy());
                assertEquals(2, features.get(1).getParams().size());
                assertEquals("2016-07-01", features.get(1).getParams().get("date"));
                assertEquals("08:30:00", features.get(1).getParams().get("time"));
                Set<String> expected = new HashSet<>();
                expected.add("GROUP1");
                expected.add("GROUP2");
                assertEquals(expected, features.get(1).getMetadata().getGroups());
                assertFalse(features.get(1).getMetadata().isEnabledByDefault());
            });
    }

    @Test
    public void getEnumFeatureMetaData() {
        contextRunner.withConfiguration(AutoConfigurations.of(
                DispatcherServletPathConfig.class,
                TogglzAutoConfiguration.class,
                TogglzEndpointAutoConfiguration.class))
            .withPropertyValues(
                "management.endpoints.web.exposure.include=*",
                "togglz.feature-enums: org.togglz.spring.boot.actuate.TogglzEndpointTest.TestFeatures")
            .run((context) -> {
                TogglzEndpoint endpoint = context.getBean(TogglzEndpoint.class);
                TogglzFeature feature = endpoint.getFeature("FEATURE_ONE");
                assertEquals("FEATURE_ONE", feature.getName());
                assertFalse( feature.isEnabled());
                assertEquals(Collections.singleton("GROUP1"), feature.getMetadata().getGroups());
                assertEquals("Feature one", feature.getMetadata().getLabel());
                assertEquals("http://togglz.org/", feature.getMetadata().getAttributes().get("InfoLink"));
                assertFalse( feature.getMetadata().isEnabledByDefault());
                endpoint.setFeatureState("FEATURE_ONE", true, null, null);
                feature = endpoint.getFeature("FEATURE_ONE");
                assertTrue( feature.isEnabled());
                assertFalse( feature.getMetadata().isEnabledByDefault());
            }).run((context) -> {
                TogglzEndpoint endpoint = context.getBean(TogglzEndpoint.class);
                TogglzFeature feature = endpoint.getFeature("FEATURE_TWO");
                assertEquals("FEATURE_TWO", feature.getName());
                assertTrue( feature.isEnabled());
                assertEquals(Collections.singleton("GROUP2"), feature.getMetadata().getGroups());
                assertEquals("Feature two", feature.getMetadata().getLabel());
                assertEquals("someguy", feature.getMetadata().getAttributes().get("Owner"));
                assertTrue( feature.getMetadata().isEnabledByDefault());
            }).run((context) -> {
                    TogglzEndpoint endpoint = context.getBean(TogglzEndpoint.class);
                    TogglzFeature feature = endpoint.getFeature("FEATURE_THREE");
                    assertEquals("FEATURE_THREE", feature.getName());
                    assertFalse( feature.isEnabled());
                    assertEquals(Collections.emptySet(), feature.getMetadata().getGroups());
                    assertEquals("FEATURE_THREE", feature.getMetadata().getLabel());
                    assertEquals(Collections.emptyMap(), feature.getMetadata().getAttributes());
                    assertFalse( feature.getMetadata().isEnabledByDefault());
            });
    }

    @Test
    public void getFeature() {
        contextRunner.withConfiguration(AutoConfigurations.of(
                DispatcherServletPathConfig.class,
                TogglzAutoConfiguration.class,
                TogglzEndpointAutoConfiguration.class))
            .withPropertyValues(
                "management.endpoints.web.exposure.include=*",
                "togglz.features.FEATURE_ONE.enabled: true",
                "togglz.features.FEATURE_TWO.enabled: false",
                "togglz.features.FEATURE_TWO.label: Feature Two",
                "togglz.features.FEATURE_TWO.groups: GROUP1,GROUP2",
                "togglz.features.FEATURE_TWO.strategy: release-date",
                "togglz.features.FEATURE_TWO.param.date: 2016-07-01",
                "togglz.features.FEATURE_TWO.param.time: 08:30:00")
            .run((context) -> {
                TogglzEndpoint endpoint = context.getBean(TogglzEndpoint.class);
                TogglzFeature feature = endpoint.getFeature("FEATURE_TWO");

                // Assert feature two
                assertEquals("FEATURE_TWO", feature.getName());
                assertFalse(feature.isEnabled());
                assertEquals("release-date", feature.getStrategy());
                assertEquals(2, feature.getParams().size());
                assertEquals("2016-07-01", feature.getParams().get("date"));
                assertEquals("08:30:00", feature.getParams().get("time"));
                Set<String> expected = new HashSet<>();
                expected.add("GROUP1");
                expected.add("GROUP2");
                assertEquals(expected, feature.getMetadata().getGroups());
                assertFalse(feature.getMetadata().isEnabledByDefault());
            });
    }

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
                    TogglzEndpoint endpoint = context.getBean(TogglzEndpoint.class);

                    // When
                    final TogglzFeature togglzFeature = endpoint.setFeatureState(
                        "FEATURE_ONE", true, null, null);

                    // Then
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
                TogglzEndpoint endpoint = context.getBean(TogglzEndpoint.class);
                String newStrategy = "aStrategy";

                // When
                final TogglzFeature togglzFeature = endpoint.setFeatureState(
                        "FEATURE_ONE", null, newStrategy, null);

                // Then
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
                    TogglzEndpoint endpoint = context.getBean(TogglzEndpoint.class);
                    String parametersString = "param1 = 10, param2 = 20 30, param3 = 40";

                    // When
                    final TogglzFeature togglzFeature = endpoint.setFeatureState(
                            "FEATURE_ONE", null, null, parametersString);

                    // Then
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
                    TogglzEndpoint endpoint = context.getBean(TogglzEndpoint.class);

                    // When
                    final TogglzFeature togglzFeature = endpoint.setFeatureState(
                        "FEATURE_ONE", false, null, null);

                    // Then
                    assertFalse(togglzFeature.isEnabled());
                });
    }

    @Test
    public void shouldThrowAnIllegalArgumentExceptionIfTheFeatureDoesNotExist() {
        contextRunner.withConfiguration(AutoConfigurations.of(
                DispatcherServletPathConfig.class,
                TogglzAutoConfiguration.class,
                TogglzEndpointAutoConfiguration.class))
                .withPropertyValues("management.endpoints.web.exposure.include=*")
                .run((context) -> {
                    // Given
                    TogglzEndpoint endpoint = context.getBean(TogglzEndpoint.class);

                    // When
                    assertThrows(IllegalArgumentException.class, () -> {
                        final TogglzFeature togglzFeature = endpoint.setFeatureState(
                            "FEATURE_ONE", false, null, null);
                    });
                });
    }

    @Test
    public void shouldThrowAnIllegalArgumentExceptionIfFormatOfParameterIsIncorrect() {
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
                    TogglzEndpoint endpoint = context.getBean(TogglzEndpoint.class);
                    String parametersString = "param1: 10";

                    // When
                    assertThrows(IllegalArgumentException.class, () -> {
                        final TogglzFeature togglzFeature = endpoint.setFeatureState(
                                "FEATURE_ONE", false, null, parametersString);
                    });
                });
    }

    @Test
    public void endpointDisabled() {
        contextRunnerWithFeatureProviderConfig()
            .withPropertyValues(
                    "management.endpoint.togglz.enabled: true")
            .run((context) -> {
                assertEquals(1, context.getBeansOfType(TogglzEndpoint.class).size());
            });
    }

}
