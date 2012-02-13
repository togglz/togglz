package org.togglz.core.util;

import java.io.IOException;
import java.io.InputStream;

public class IOUtils {

    public static void close(InputStream s) {
        if (s != null) {
            try {
                s.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

}
