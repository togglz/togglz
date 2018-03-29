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

package org.togglz.spring.boot1.autoconfigure.actuator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.togglz.spring.boot.autoconfigure.TogglzAutoConfiguration;
import org.togglz.spring.boot1.autoconfigure.BaseTest;
import org.togglz.spring.boot1.autoconfigure.actuator.TogglzEndpoint;

/**
 * Tests for {@link TogglzAutoConfiguration}.
 *
 * @author Marcel Overdijk
 */
public class TogglzEndpointAutoConfigurationTest extends BaseTest {

    @Test
    public void defaultTogglzEndpoint() {
        loadWithDefaults(new Class[]{FeatureProviderConfig.class});

        TogglzEndpoint togglzEndpoint = this.context.getBean(TogglzEndpoint.class);
        assertEquals("togglz", togglzEndpoint.getId());
        assertTrue(togglzEndpoint.isEnabled());
        assertTrue(togglzEndpoint.isSensitive());
    }
}
