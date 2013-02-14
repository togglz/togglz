package org.togglz.core.repository.util;

import java.util.Map;

public interface MapSerializer {

    /**
     * Converts the given map to a string representation that can be used to restore the map using {@link #deserialize(String)}.
     */
    String serialize(Map<String, String> map);

    /**
     * Restores a map that has been converted into a string using {@link #serialize(Map)}
     */
    Map<String, String> deserialize(String data);

}