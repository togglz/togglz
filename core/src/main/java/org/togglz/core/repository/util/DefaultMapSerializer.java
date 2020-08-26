package org.togglz.core.repository.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * This converter is able to convert string maps to simple strings and vice versa.
 * 
 * @author Christian Kaltepoth
 */
public class DefaultMapSerializer implements MapSerializer {

    private final boolean multiline;

    private final String lineSeparator;

    /**
     * Use {@link #create()}
     */
    private DefaultMapSerializer(boolean multiline) {
        this(multiline, "\n");
    }

    /**
     * Use {@link #create()}
     */
    private DefaultMapSerializer(boolean multiline, String lineSeparator) {
        this.multiline = multiline;
        this.lineSeparator = lineSeparator;
    }

    /**
     * Creates a new instance of the {@link DefaultMapSerializer}.
     */
    public static DefaultMapSerializer singleline() {
        return new DefaultMapSerializer(false);
    }

    /**
     * Creates a new instance of the {@link DefaultMapSerializer}.
     */
    public static DefaultMapSerializer multiline() {
        return new DefaultMapSerializer(true);
    }

    /**
     * Creates a new instance of the {@link DefaultMapSerializer}.
     */
    public static DefaultMapSerializer multiline(String lineSeparator) {
        return new DefaultMapSerializer(true, lineSeparator);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.togglz.core.util.MapSerializer#convertToString(java.util.Map)
     */
    @Override
    public String serialize(Map<String, String> map) {

        try {

            // the format is based on the properties output format
            Properties props = new Properties();
            props.putAll(map);
            Writer writer = new StringWriter();
            props.store(writer, null);

            // we need a list of strings so we can further process the format
            List<String> lines = new ArrayList<String>();
            for (String line : writer.toString().split("\r?\n")) {
                // comments and empty lines are skipped
                if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
                    lines.add(line.trim());
                }
            }

            // sort by key so create a canonical format
            Collections.sort(lines);

            // build the output string
            StringBuilder builder = new StringBuilder();
            for (String line : lines) {

                if (multiline) {
                    if (builder.length() > 0) {
                        builder.append(lineSeparator);
                    }
                    builder.append(line);
                }
                else {
                    if (builder.length() > 0) {
                        builder.append("&");
                    }
                    builder.append(line.replace("&", "\\u0026"));
                }

            }
            return builder.toString();

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.togglz.core.util.MapSerializer#convertFromString(java.lang.String)
     */
    @Override
    public Map<String, String> deserialize(String s) {

        try {

            String input = multiline ? s : s.replace('&', '\n');

            Properties props = new Properties();
            if (s != null) {
                props.load(new StringReader(input));
            }

            LinkedHashMap<String, String> result = new LinkedHashMap<>();
            for (Entry<Object, Object> entry : props.entrySet()) {
                result.put(entry.getKey().toString(), entry.getValue().toString());
            }
            return result;

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }

}
