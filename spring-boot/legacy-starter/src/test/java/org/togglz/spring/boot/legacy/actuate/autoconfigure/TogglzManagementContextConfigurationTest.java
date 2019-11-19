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

package org.togglz.spring.boot.legacy.actuate.autoconfigure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.togglz.spring.boot.legacy.actuate.BaseTest;

/**
 * Tests for {@link TogglzAutoConfiguration}.
 *
 * @author Marcel Overdijk
 */
public class TogglzManagementContextConfigurationTest extends BaseTest {

    @Test
    public void consoleWithCustomManagementContextPath() {
        // With TogglzManagementContextConfiguration responsible for creating the admin console servlet registration bean,
        // if a custom managememnt context path is provided it should be used as prefix.
        loadWithDefaults(new Class[]{FeatureProviderConfig.class},
                "management.context-path: /manage");
        assertEquals(1, this.context.getBeansOfType(ServletRegistrationBean.class).size());
        assertTrue(this.context.getBean(ServletRegistrationBean.class).getUrlMappings().contains("/manage/togglz-console/*"));
    }

    @Test
    public void customConsolePath() {
        loadWithDefaults(new Class[]{FeatureProviderConfig.class},
                "togglz.console.path: /custom");
        assertEquals(1, this.context.getBeansOfType(ServletRegistrationBean.class).size());
        assertTrue(this.context.getBean(ServletRegistrationBean.class).getUrlMappings().contains("/custom/*"));
    }

    @Test
    public void customConsolePathWithTrailingSlash() {
        loadWithDefaults(new Class[]{FeatureProviderConfig.class},
                "togglz.console.path: /custom/");
        assertEquals(1, this.context.getBeansOfType(ServletRegistrationBean.class).size());
        assertTrue(this.context.getBean(ServletRegistrationBean.class).getUrlMappings().contains("/custom/*"));
    }

}
