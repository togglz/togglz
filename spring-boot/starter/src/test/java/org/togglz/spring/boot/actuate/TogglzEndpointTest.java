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

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzEndpointAutoConfiguration;
import org.togglz.spring.boot.autoconfigure.TogglzAutoConfiguration;
import org.togglz.spring.boot.autoconfigure.TogglzFeature;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Spring Boot 2 compatible {@link TogglzEndpoint}.
 *
 * @author Marcel Overdijk
 * @author Rui Figueira
 */
public class TogglzEndpointTest extends BaseTest {

    @Test
    public void getAllFeatures() throws Exception {
        contextRunner.withConfiguration(AutoConfigurations.of(
                TogglzAutoConfiguration.class,
                TogglzEndpointAutoConfiguration.class))
            .withPropertyValues(
                    "togglz.features.FEATURE_ONE.enabled: true",
                    "togglz.features.FEATURE_TWO.enabled: false",
                    "togglz.features.FEATURE_TWO.strategy: release-date",
                    "togglz.features.FEATURE_TWO.param.date: 2016-07-01",
                    "togglz.features.FEATURE_TWO.param.time: 08:30:00")
            .run((context) -> {
                TogglzEndpoint endpoint = context.getBean(TogglzEndpoint.class);
                List<TogglzFeature> features = endpoint.getAllFeatures();

                // Assert we have 2 features
                assertThat(features).hasSize(2);

                // Assert feature one
                assertThat(features.get(0).getName()).isEqualTo("FEATURE_ONE");
                assertThat(features.get(0).isEnabled()).isTrue();
                assertThat(features.get(0).getStrategy()).isNull();
                assertThat(features.get(0).getParams()).isEmpty();

                // Assert feature two
                assertThat(features.get(1).getName()).isEqualTo("FEATURE_TWO");
                assertThat(features.get(1).isEnabled()).isFalse();
                assertThat(features.get(1).getStrategy()).isEqualTo("release-date");
                assertThat(features.get(1).getParams()).hasSize(2);
                assertThat(features.get(1).getParams().get("date")).isEqualTo("2016-07-01");
                assertThat(features.get(1).getParams().get("time")).isEqualTo("08:30:00");
            });
    }

    @Test
    public void shouldEnableAFeature() {
        contextRunner.withConfiguration(AutoConfigurations.of(
                TogglzAutoConfiguration.class,
                TogglzEndpointAutoConfiguration.class))
                .withPropertyValues(
                        "togglz.features.FEATURE_ONE.enabled: false")
                .run((context) -> {
                    // Given
                    TogglzEndpoint endpoint = context.getBean(TogglzEndpoint.class);

                    // When
                    final TogglzFeature togglzFeature = endpoint.setFeatureState("FEATURE_ONE", true);

                    // Then
                    assertThat(togglzFeature.isEnabled()).isTrue();
                });
    }

    @Test
    public void shouldDisableAFeature() {
        contextRunner.withConfiguration(AutoConfigurations.of(
                TogglzAutoConfiguration.class,
                TogglzEndpointAutoConfiguration.class))
                .withPropertyValues(
                        "togglz.features.FEATURE_ONE.enabled: true")
                .run((context) -> {
                    // Given
                    TogglzEndpoint endpoint = context.getBean(TogglzEndpoint.class);

                    // When
                    final TogglzFeature togglzFeature = endpoint.setFeatureState("FEATURE_ONE", false);

                    // Then
                    assertThat(togglzFeature.isEnabled()).isFalse();
                });
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAnIllegalArgumentExceptionIfTheFeatureDoesNotExist() {
        contextRunner.withConfiguration(AutoConfigurations.of(
                TogglzAutoConfiguration.class,
                TogglzEndpointAutoConfiguration.class))
                .run((context) -> {
                    // Given
                    TogglzEndpoint endpoint = context.getBean(TogglzEndpoint.class);

                    // When
                    final TogglzFeature togglzFeature = endpoint.setFeatureState("FEATURE_ONE", false);
                });
    }

    @Test
    public void endpointDisabled() {
        contextRunnerWithFeatureProviderConfig()
            .withPropertyValues("management.endpoint.togglz.enabled: false")
            .run((context) -> {
                assertThat(context.getBeansOfType(TogglzEndpoint.class)).isEmpty();
            });
    }

}
