package org.togglz.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.togglz.core.util.Strings;

public class Togglz {

    /**
     * Returns <code>Togglz</code> followed by the version if available
     */
    public static String getNameWithVersion() {
        String version = getVersion();
        return version != null ? "Togglz " + version : "Togglz";
    }

    /**
     * The version of Togglz or <code>null</code> if it cannot be identified
     */
    public static String getVersion() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = Togglz.class.getClassLoader();
        }

        URL url = classLoader.getResource("META-INF/maven/org.togglz/togglz-core/pom.properties");
        if (url == null) {
            return null;
        }

        try (InputStream stream = url.openStream()) {
            Properties props = new Properties();
            props.load(stream);
            return Strings.trimToNull(props.getProperty("version"));
        } catch (IOException e) {
            // ignore
        }
        return null;
    }
}
