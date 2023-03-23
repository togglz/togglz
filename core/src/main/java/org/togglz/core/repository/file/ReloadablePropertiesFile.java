package org.togglz.core.repository.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.repository.property.PropertySource;
import org.togglz.core.util.IOUtils;

/**
 * Please note that this class is NOT thread-safe.
 */
class ReloadablePropertiesFile implements PropertySource {

    private final Logger log = LoggerFactory.getLogger(ReloadablePropertiesFile.class);

    private final File file;

    private final int minCheckInterval;

    private Properties values = new Properties();

    private long lastRead = 0;

    private long lastCheck = 0;

    public ReloadablePropertiesFile(File file, int minCheckInterval) {
        this.file = file;
        this.minCheckInterval = minCheckInterval;
    }

    public synchronized void reloadIfUpdated() {
        if (!this.file.exists()) {
            try {
                if (this.file.createNewFile()) {
                    log.debug("Created non-existent file.");
                }
            } catch (IOException e) {
                log.error("Error creating missing file " + this.file.getName(), e);
            }
        }

        long now = System.currentTimeMillis();
        if (now - lastCheck > minCheckInterval) {

            lastCheck = now;

            if (file.lastModified() > lastRead) {

                FileInputStream stream = null;

                try {

                    // read new values
                    stream = new FileInputStream(file);
                    Properties newValues = new Properties();
                    newValues.load(stream);

                    // update state
                    values = newValues;
                    lastRead = System.currentTimeMillis();

                    log.info("Reloaded file: " + file.getCanonicalPath());

                } catch (FileNotFoundException e) {
                    log.debug("File not found: " + file);
                } catch (IOException e) {
                    log.error("Failed to read file", e);
                } finally {
                    IOUtils.close(stream);
                }

            }
        }

    }

    public String getValue(String key, String defaultValue) {
        return values.getProperty(key, defaultValue);
    }

    public Set<String> getKeysStartingWith(String prefix) {
        Set<String> result = new HashSet<>();

        Enumeration<?> keys = values.propertyNames();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            if (key.startsWith(prefix)) {
                result.add(key);
            }
        }
        return result;
    }

    public PropertySource.Editor getEditor() {
        return new PropertyFileEditor(values);
    }

    private void write(Properties newValues) {
        try {

            FileOutputStream fos = new FileOutputStream(file);
            newValues.store(fos, null);
            fos.flush();
            fos.close();

        } catch (IOException e) {
            throw new IllegalStateException("Failed to write new values", e);
        }
        lastRead = 0;
        lastCheck = 0;
    }

    private class PropertyFileEditor implements PropertySource.Editor {

        private final Properties newValues;

        private PropertyFileEditor(Properties props) {
            newValues = new Properties();
            newValues.putAll(props);
        }

        public void setValue(String key, String value) {
            if (value != null) {
                newValues.setProperty(key, value);
            } else {
                newValues.remove(key);
            }
        }

        public void removeKeysStartingWith(String prefix) {
            newValues.entrySet().removeIf(entry -> entry.getKey().toString().startsWith(prefix));
        }

        public void commit() {
            write(newValues);
        }
    }
}
