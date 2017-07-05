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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Tests for {@link TogglzEndpoint}.
 *
 * @author Marcel Overdijk
 */
public class TogglzEndpointTest {

    private AnnotationConfigApplicationContext context;

    @After
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void invoke() throws Exception {
        load(new Class[]{JacksonAutoConfiguration.class, TogglzAutoConfiguration.class},
                "togglz.features.FEATURE_ONE.enabled: true",
                "togglz.features.FEATURE_TWO.enabled: false",
                "togglz.features.FEATURE_TWO.strategy: release-date",
                "togglz.features.FEATURE_TWO.param.date: 2016-07-01",
                "togglz.features.FEATURE_TWO.param.time: 08:30:00");

        TogglzEndpoint endpoint = this.context.getBean(TogglzEndpoint.class);
        List<TogglzEndpoint.TogglzFeature> features = endpoint.invoke();

        // Assert we have 2 features
        assertEquals(2, features.size());

        // Assert feature one
        assertEquals("FEATURE_ONE", features.get(0).getName());
        assertTrue(features.get(0).isEnabled());
        assertNull(features.get(0).getStrategy());
        assertEquals(0, features.get(0).getParams().size());

        // Assert feature two
        assertEquals("FEATURE_TWO", features.get(1).getName());
        assertFalse(features.get(1).isEnabled());
        assertEquals("release-date", features.get(1).getStrategy());
        assertEquals(2, features.get(1).getParams().size());
        assertEquals("2016-07-01", features.get(1).getParams().get("date"));
        assertEquals("08:30:00", features.get(1).getParams().get("time"));
    }

    private void load(Class<?>[] configs, String... environment) {
        this.context = new AnnotationConfigApplicationContext();
        this.context.register(configs);
        EnvironmentTestUtils.addEnvironment(this.context, environment);
        this.context.refresh();
    }

}
