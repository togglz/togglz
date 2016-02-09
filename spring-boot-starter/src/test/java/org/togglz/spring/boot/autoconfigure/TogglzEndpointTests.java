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

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.Feature;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.spi.FeatureProvider;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link TogglzEndpoint}.
 *
 * @author Marcel Overdijk
 */
public class TogglzEndpointTests {

    private AnnotationConfigApplicationContext context;

    @After
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void invoke() throws Exception {
        load(new Class[]{JacksonAutoConfiguration.class, TogglzAutoConfiguration.class, FeatureProviderConfig.class},
                "togglz.features.FEATURE_ONE: true",
                "togglz.features.FEATURE_TWO: false",
                "togglz.features.FEATURE_TWO.strategy: release-date",
                "togglz.features.FEATURE_TWO.param.date: 2016-07-01",
                "togglz.features.FEATURE_TWO.param.time: 08:30:00");
        TogglzEndpoint endpoint = this.context.getBean(TogglzEndpoint.class);
        List<TogglzEndpoint.TogglzFeature> features = endpoint.invoke();
        assertThat(features.size(), is(2));
        assertThat(features.get(0).getName(), is("FEATURE_ONE"));
        assertThat(features.get(0).isEnabled(), is(true));
        assertThat(features.get(0).getStrategy(), is(nullValue()));
        assertThat(features.get(0).getParams().size(), is(0));
        assertThat(features.get(1).getName(), is("FEATURE_TWO"));
        assertThat(features.get(1).isEnabled(), is(false));
        assertThat(features.get(1).getStrategy(), is("release-date"));
        assertThat(features.get(1).getParams().size(), is(2));
        assertThat(features.get(1).getParams().get("date"), is("2016-07-01"));
        assertThat(features.get(1).getParams().get("time"), is("08:30:00"));
    }

    private void load(Class<?>[] configs, String... environment) {
        this.context = new AnnotationConfigApplicationContext();
        this.context.register(configs);
        EnvironmentTestUtils.addEnvironment(this.context, environment);
        this.context.refresh();
    }

    protected enum MyFeatures implements Feature {
        FEATURE_ONE,
        FEATURE_TWO;
    }

    @Configuration
    protected static class FeatureProviderConfig {

        @Bean
        public FeatureProvider featureProvider() {
            return new EnumBasedFeatureProvider(MyFeatures.class);
        }
    }
}
