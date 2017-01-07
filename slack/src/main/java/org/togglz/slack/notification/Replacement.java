package org.togglz.slack.notification;

import java.util.Map;

class Replacement {

    private final Map<String, String> values;

    private final String prefix;

    Replacement(final Map<String, String> values, final String prefix) {
        this.values = values;
        this.prefix = prefix;
    }

    String replace(String source) {
        String result = source;
        for (String key : values.keySet()) {
            String from = prefix + key;
            String to = values.get(key);
            result = result.replace(from, to);
        }
        return result;
    }
}
