package org.togglz.core.repository.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.data.MapEntry;
import org.junit.Test;

public class DefaultMapSerializerTest {

    @Test
    public void shouldConvertInMultilineMode() {

        DefaultMapSerializer serializer = DefaultMapSerializer.multiline("\n");

        Map<String, String> input = new HashMap<String, String>();
        input.put("param1", "value1");
        input.put("param2", "value2");

        String data = serializer.serialize(input);

        assertThat(data)
            .isEqualTo("param1=value1\nparam2=value2");

        Map<String, String> result = serializer.deserialize(data);

        assertThat(result)
            .hasSize(2)
            .contains(MapEntry.entry("param1", "value1"))
            .contains(MapEntry.entry("param2", "value2"));

    }

    @Test
    public void shouldConvertInSinglelineMode() {

        DefaultMapSerializer serializer = DefaultMapSerializer.singleline();

        Map<String, String> input = new HashMap<String, String>();
        input.put("param1", "value1");
        input.put("param2", "value2");

        String data = serializer.serialize(input);

        assertThat(data)
            .isEqualTo("param1=value1&param2=value2");

        Map<String, String> result = serializer.deserialize(data);

        assertThat(result)
            .hasSize(2)
            .contains(MapEntry.entry("param1", "value1"))
            .contains(MapEntry.entry("param2", "value2"));

    }

    @Test
    public void escapesAmpersandInSinglelineMode() {

        DefaultMapSerializer serializer = DefaultMapSerializer.singleline();

        Map<String, String> input = new HashMap<String, String>();
        input.put("param1", "foo&bar");

        String data = serializer.serialize(input);

        assertThat(data)
            .isEqualTo("param1=foo\\u0026bar");

        Map<String, String> result = serializer.deserialize(data);

        assertThat(result)
            .hasSize(1)
            .contains(MapEntry.entry("param1", "foo&bar"));

    }

    @Test
    public void escapesNewLineInSinglelineMode() {

        DefaultMapSerializer serializer = DefaultMapSerializer.singleline();

        Map<String, String> input = new HashMap<String, String>();
        input.put("param1", "foo\r\nbar");

        String data = serializer.serialize(input);

        assertThat(data)
            .isEqualTo("param1=foo\\r\\nbar");

        Map<String, String> result = serializer.deserialize(data);

        assertThat(result)
            .hasSize(1)
            .contains(MapEntry.entry("param1", "foo\r\nbar"));

    }

    @Test
    public void escapesNewLineInMultilineMode() {

        DefaultMapSerializer serializer = DefaultMapSerializer.multiline("\n");

        Map<String, String> input = new HashMap<String, String>();
        input.put("param1", "foo\r\nbar");

        String data = serializer.serialize(input);

        assertThat(data)
            .isEqualTo("param1=foo\\r\\nbar");

        Map<String, String> result = serializer.deserialize(data);

        assertThat(result)
            .hasSize(1)
            .contains(MapEntry.entry("param1", "foo\r\nbar"));

    }

    @Test
    public void lineSeparatorShouldDefaultSimpleNewLine() {

        DefaultMapSerializer serializer = DefaultMapSerializer.multiline();

        Map<String, String> input = new HashMap<String, String>();
        input.put("param1", "value1");
        input.put("param2", "value2");

        String data = serializer.serialize(input);

        assertThat(data)
            .isEqualTo("param1=value1\nparam2=value2");

    }


    @Test
    public void shouldUseCustomLineSeparator() {

        DefaultMapSerializer serializer = DefaultMapSerializer.multiline("%");

        Map<String, String> input = new HashMap<String, String>();
        input.put("param1", "value1");
        input.put("param2", "value2");

        String data = serializer.serialize(input);

        assertThat(data)
            .isEqualTo("param1=value1%param2=value2");

    }

}
