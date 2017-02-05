package org.togglz.deltaspike;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.spi.config.ConfigSourceProvider;

public class TestConfigSourceProvider implements ConfigSourceProvider {

    private static final ConfigSource configSource = new ConfigSource() {

        @Override
        public int getOrdinal() {
            return 0;
        }

        @Override
        public Map<String, String> getProperties() {
            return properties;
        }

        @Override
        public String getPropertyValue(String key) {
            return getProperty(key);
        }

        @Override
        public String getConfigName() {
            return "test";
        }

        @Override
        public boolean isScannable() {
            return true;
        }
    };
    private static final Map<String, String> properties = new HashMap<>();

    public static void clearProperties() {
        properties.clear();
    }

    public static String getProperty(String key) {
        return properties.get(key);
    }

    public static void putProperty(String key, String value) {
        properties.put(key, value);
    }

    public static void removeProperty(String key) {
        properties.remove(key);
    }

    @Override
    public List<ConfigSource> getConfigSources() {
        return Collections.singletonList(configSource);
    }
}
