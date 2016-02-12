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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.togglz.core.Feature;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Configuration properties for Togglz.
 *
 * @author Marcel Overdijk
 */
@ConfigurationProperties(prefix = "togglz", ignoreUnknownFields = true)
public class TogglzProperties {

    private boolean enabled = true;

    private Class<? extends Feature>[] featureEnums;

    private String featureManagerName;

    private Map<String, String> features;

    private String featuresFile;

    private Integer featuresFileMinCheckInterval;

    private Cache cache = new Cache();

    @Valid
    private Console console = new Console();

    private Endpoint endpoint = new Endpoint();

    public boolean isEnabled() {
        return enabled;
    }

    public Class<? extends Feature>[] getFeatureEnums() {
        return featureEnums;
    }

    public void setFeatureEnums(Class<? extends Feature>[] featureEnums) {
        this.featureEnums = featureEnums;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFeatureManagerName() {
        return featureManagerName;
    }

    public void setFeatureManagerName(String featureManagerName) {
        this.featureManagerName = featureManagerName;
    }

    public Map<String, String> getFeatures() {
        return features;
    }

    public void setFeatures(Map<String, String> features) {
        this.features = features;
    }

    public String getFeaturesFile() {
        return featuresFile;
    }

    public void setFeaturesFile(String featuresFile) {
        this.featuresFile = featuresFile;
    }

    public Integer getFeaturesFileMinCheckInterval() {
        return featuresFileMinCheckInterval;
    }

    public void setFeaturesFileMinCheckInterval(Integer featuresFileMinCheckInterval) {
        this.featuresFileMinCheckInterval = featuresFileMinCheckInterval;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public Console getConsole() {
        return console;
    }

    public void setConsole(Console console) {
        this.console = console;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public static class Cache {

        private boolean enabled = false;

        private long timeToLive = 0;

        private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getTimeToLive() {
            return timeToLive;
        }

        public void setTimeToLive(long timeToLive) {
            this.timeToLive = timeToLive;
        }

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }

        public void setTimeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
        }
    }

    public static class Console {

        private boolean enabled = true;

        @NotNull
        @Pattern(regexp = "/[^?#]*", message = "Path must start with /")
        private String path = "/togglz-console";

        private String featureAdminAuthority;

        private boolean secured = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getFeatureAdminAuthority() {
            return featureAdminAuthority;
        }

        public void setFeatureAdminAuthority(String featureAdminAuthority) {
            this.featureAdminAuthority = featureAdminAuthority;
        }

        public boolean isSecured() {
            return secured;
        }

        public void setSecured(boolean secured) {
            this.secured = secured;
        }
    }

    public static class Endpoint {

        private String id = "togglz";

        private boolean enabled = true;

        private boolean sensitive = true;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isSensitive() {
            return sensitive;
        }

        public void setSensitive(boolean sensitive) {
            this.sensitive = sensitive;
        }
    }
}
