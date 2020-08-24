package org.togglz.microprofile;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singleton;

/**
 * A test config source provider for MicroProfile Config
 */
public class TestConfigSourceProvider implements ConfigSourceProvider {

    @Override
    public Iterable<ConfigSource> getConfigSources(ClassLoader classLoader) {
        return singleton(TestConfigSource.INSTANCE);
    }

    public static class TestConfigSource implements ConfigSource {
        public static TestConfigSource INSTANCE = new TestConfigSource();
        private Map<String, String> properties = new HashMap<>();

        @Override
        public Map<String, String> getProperties() {
            return properties;
        }

        @Override
        public String getValue(String name) {
            return properties.get(name);
        }

        @Override
        public String getName() {
            return "microprofile-config-test-source";
        }

        public void clearProperties() {
            properties.clear();
        }

        public void putProperty(String key, String value) {
            properties.put(key, value);
        }
    }
}
