package org.togglz.core.repository.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultMapSerializerTest {

    @Test
    public void shouldConvertInMultilineMode() {

        DefaultMapSerializer serializer = DefaultMapSerializer.multiline("\n");

        Map<String, String> input = new HashMap<>();
        input.put("param1", "value1");
        input.put("param2", "value2");

        String data = serializer.serialize(input);

        assertEquals("param1=value1\nparam2=value2", data);

        Map<String, String> result = serializer.deserialize(data);

        assertEquals(2, result.size());
        assertTrue(result.containsKey("param1"));
        assertTrue(result.containsValue("value1"));

        assertTrue(result.containsKey("param2"));
        assertTrue(result.containsValue("value2"));
    }

    @Test
    public void shouldConvertInSinglelineMode() {

        DefaultMapSerializer serializer = DefaultMapSerializer.singleline();

        Map<String, String> input = new HashMap<>();
        input.put("param1", "value1");
        input.put("param2", "value2");

        String data = serializer.serialize(input);

        assertEquals("param1=value1&param2=value2", data);

        Map<String, String> result = serializer.deserialize(data);

        assertEquals(2, result.size());
        assertTrue(result.containsKey("param1"));
        assertTrue(result.containsValue("value1"));

        assertTrue(result.containsKey("param2"));
        assertTrue(result.containsValue("value2"));
    }

    @Test
    public void escapesAmpersandInSinglelineMode() {

        DefaultMapSerializer serializer = DefaultMapSerializer.singleline();

        Map<String, String> input = new HashMap<>();
        input.put("param1", "foo&bar");

        String data = serializer.serialize(input);

        assertEquals("param1=foo\\u0026bar", data);

        Map<String, String> result = serializer.deserialize(data);

        assertEquals(1, result.size());

        assertTrue(result.containsKey("param1"));
        assertTrue(result.containsValue("foo&bar"));
    }

    @Test
    public void escapesNewLineInSinglelineMode() {

        DefaultMapSerializer serializer = DefaultMapSerializer.singleline();

        Map<String, String> input = new HashMap<>();
        input.put("param1", "foo\r\nbar");

        String data = serializer.serialize(input);

        assertEquals("param1=foo\\r\\nbar", data);

        Map<String, String> result = serializer.deserialize(data);

        assertEquals(1, result.size());
        assertTrue(result.containsKey("param1"));
        assertTrue(result.containsValue("foo\r\nbar"));
    }

    @Test
    public void escapesNewLineInMultilineMode() {

        DefaultMapSerializer serializer = DefaultMapSerializer.multiline("\n");

        Map<String, String> input = new HashMap<>();
        input.put("param1", "foo\r\nbar");

        String data = serializer.serialize(input);

        assertEquals("param1=foo\\r\\nbar", data);

        Map<String, String> result = serializer.deserialize(data);

        assertEquals(1, result.size());
        assertTrue(result.containsKey("param1"));
        assertTrue(result.containsValue("foo\r\nbar"));
    }

    @Test
    public void lineSeparatorShouldDefaultSimpleNewLine() {

        DefaultMapSerializer serializer = DefaultMapSerializer.multiline();

        Map<String, String> input = new HashMap<>();
        input.put("param1", "value1");
        input.put("param2", "value2");

        String data = serializer.serialize(input);

        assertEquals("param1=value1\nparam2=value2", data);
    }

    @Test
    public void shouldUseCustomLineSeparator() {

        DefaultMapSerializer serializer = DefaultMapSerializer.multiline("%");

        Map<String, String> input = new HashMap<>();
        input.put("param1", "value1");
        input.put("param2", "value2");

        String data = serializer.serialize(input);

        assertEquals("param1=value1%param2=value2", data);
    }
}
