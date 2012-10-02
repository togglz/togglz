package org.togglz.core.repository.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.util.IOUtils;

class ReloadablePropertiesFile {

    private final Log log = LogFactory.getLog(ReloadablePropertiesFile.class);

    private final File file;

    private Properties values = new Properties();

    private long lastRead = 0;

    public ReloadablePropertiesFile(File file) {
        this.file = file;
    }

    public synchronized void reloadIfUpdated() {

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

    public String getValue(String key, String defaultValue) {
        return values.getProperty(key, defaultValue);
    }
    
    public Editor getEditor() {
        return new Editor(values);
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

    }

    public class Editor {

        private Properties newValues;

        private Editor(Properties props) {
            newValues = new Properties();
            newValues.putAll(props);
        }

        public void setValue(String key, String value) {
            newValues.setProperty(key, value);
        }

        public void removeValue(String usersKey) {
            newValues.remove(usersKey);
        }
        
        public void commit() {
            write(newValues);
        }

    }

}
