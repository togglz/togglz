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

package org.togglz.spring.boot.actuate.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.NamedFeature;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Configuration properties for Togglz.
 *
 * @author Marcel Overdijk
 */
@ConfigurationProperties(prefix = "togglz", ignoreUnknownFields = true)
@Validated
public class TogglzProperties {

	/**
	 * Enable Togglz for the application.
	 */
	private boolean enabled = true;

	/**
	 * Optional comma-separated list of fully-qualified feature enum class
	 * names. Features can also be specified using the features property.
	 */
	private Class<? extends Feature>[] featureEnums;

	/**
	 * The name of the feature manager.
	 */
	private String featureManagerName;

	/**
	 * The features and their states. Only needed if feature states are stored
	 * in application properties.
	 */
	private Map<String, FeatureSpec> features = new LinkedHashMap<>();

	/**
	 * The path to the features file that contains the feature states. Only
	 * needed if feature states are stored in external properties file.
	 */
	private String featuresFile;

	/**
	 * Enable auto creation of features file if it does not exists.
	 */
	private boolean createFeaturesFileIfAbsent = true;

	/**
	 * The minimum amount of time in milliseconds to wait between checks of the
	 * file's modification date.
	 */
	private Integer featuresFileMinCheckInterval;

	/**
	 * Feature state caching.
	 */
	private Cache cache = new Cache();

	@Valid
	private Console console = new Console();

	/**
	 * Togglz actuator endpoint.
	 */
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

	public Map<String, FeatureSpec> getFeatures() {
		return features;
	}

	public void setFeatures(Map<String, FeatureSpec> features) {
		this.features = features;
	}

	public String getFeaturesFile() {
		return featuresFile;
	}

	public void setFeaturesFile(String featuresFile) {
		this.featuresFile = featuresFile;
	}

	public boolean isCreateFeaturesFileIfAbsent() { return createFeaturesFileIfAbsent; }

	public void setCreateFeaturesFileIfAbsent(boolean createFeaturesFileIfAbsent) { this.createFeaturesFileIfAbsent = createFeaturesFileIfAbsent; }

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

	public static class FeatureSpec {
		/**
		 * Flag to say that the feature is enabled.
		 */
		private boolean enabled;

		/**
		 * Label for the feature in a UI.
		 */
		private String label;

		/**
		 * Optional strategy ID to identify the activation strategy to use for this feature.
		 */
		private String strategy;

		/**
		 * Names of the groups that this feature belongs to (optional).
		 */
		private final Set<String> groups = new LinkedHashSet<>();

		/**
		 * Parameters that can be used by the activation strategy.
		 */
		private final Map<String, String> param = new LinkedHashMap<>();

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getStrategy() {
			return strategy;
		}

		public void setStrategy(String strategy) {
			this.strategy = strategy;
		}

		public Set<String> getGroups() {
			return groups;
		}

		public Map<String, String> getParam() {
			return param;
		}

		public FeatureState state(String name) {
			FeatureState state = new FeatureState(feature(name), enabled);
			for (String key : param.keySet()) {
				state.setParameter(key, param.get(key));
			}
			if (StringUtils.hasText(strategy)) {
				state.setStrategyId(strategy);
			}
			return state;
		}

		public Feature feature(String name) {
			return new NamedFeature(name);
		}

		public String spec() {
			return (label == null ? "" : label) + ";" + enabled
					+ (groups.isEmpty() ? "" : ";" + StringUtils.collectionToCommaDelimitedString(groups));
		}

		@Override
		public String toString() {
			return "FeatureSpec [label=" + label + ", enabled=" + enabled + ", groups=" + groups + ", strategy="
					+ strategy + ", param=" + param + "]";
		}
	}

	public static class Cache {

		/**
		 * Enable feature state caching.
		 */
		private boolean enabled = false;

		/**
		 * The time after which a cache entry will expire.
		 * 0 means it will not expire as long as the feature state does not get modified.
		 */
		private long timeToLive = 0;

		/**
		 * The time unit as java.util.concurrent.TimeUnit enum name (one of
		 * "nanoseconds", "microseconds", "milliseconds", "seconds", "minutes",
		 * "hours", "days").
		 */
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

		/**
		 * @param timeToLive time in milliseconds after which the cache will expire and the value will get reloaded.
		 *                   Setting this to 0 will never expire the cache unless the feature state gets modified.
		 *                   Negative values are not allowed.
		 */
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

		/**
		 * Enable admin console.
		 */
		private boolean enabled = true;

		/**
		 * The path of the admin console when enabled.
		 */
		@NotNull
		@Pattern(regexp = "/[^?#]*", message = "Path must start with /")
		private String path = "/togglz-console";

		/**
		 * The name of the authority that is allowed to access the admin
		 * console.
		 */
		private String featureAdminAuthority;

		/**
		 * Indicates if the admin console runs in secured mode. If false the
		 * application itself should take care of securing the admin console.
		 */
		private boolean secured = true;

		/**
		 * Validates the csrf token during toggle update for a single instance.
		 */
		private boolean validateCSRFToken = true;

		/**
		 * Indicates if the admin console runs on the management port.
		 */
		private boolean useManagementPort = true;

		public boolean isValidateCSRFToken() {
			return validateCSRFToken;
		}

		public void setValidateCSRFToken(boolean validateCSRFToken) {
			this.validateCSRFToken = validateCSRFToken;
		}

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

		public boolean isUseManagementPort() {
			return useManagementPort;
		}

		public void setUseManagementPort(boolean useManagementPort) {
			this.useManagementPort = useManagementPort;
		}
	}

	public static class Web {

		/**
		 * Register the FeatureInterceptor that allows you to activate a
		 * controller or controller methods based on features.
		 */
		private boolean registerFeatureInterceptor = false;

		public boolean isRegisterFeatureInterceptor() {
			return registerFeatureInterceptor;
		}

		public void setRegisterFeatureInterceptor(boolean registerFeatureInterceptor) {
			this.registerFeatureInterceptor = registerFeatureInterceptor;
		}
	}

	public static class Endpoint {

		/**
		 * The endpoint identifier.
		 */
		private String id = "togglz";

		/**
		 * Enable actuator endpoint.
		 */
		private boolean enabled = true;

		/**
		 * Indicates if the endpoint exposes sensitive information.
		 */
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

	/**
	 * The configured features in a format that can be consumed by a
	 * PropertyFeatureProvider.
	 *
	 * @return features in the right format.
	 */
	public Properties getFeatureProperties() {
		Properties properties = new Properties();
		for (String name : features.keySet()) {
			properties.setProperty(name, features.get(name).spec());
		}
		return properties;
	}
}
