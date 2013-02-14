package org.togglz.core.repository.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.togglz.core.repository.util.DefaultMapSerializer;

public class DefaultMapSerializerTest {

    @Test
    public void testWithNewLines() {

        DefaultMapSerializer persister = DefaultMapSerializer.create().withNewLines();

        Map<String, String> input = new HashMap<String, String>();
        input.put("param1", "a&b c");
        input.put("param2", "a\r\nb");

        /*
         * & is not escaped, \r and \n are escaped, parameters are divided by \r\n
         */
        String str = persister.serialize(input);
        assertThat(str, is("param1=a&b c\r\nparam2=a\\r\\nb\r\n"));

        Map<String, String> output = persister.deserialize(str);
        assertEquals(input, output);

    }

    @Test
    public void testWithoutNewLines() {

        DefaultMapSerializer persister = DefaultMapSerializer.create().withoutNewLines();

        Map<String, String> input = new HashMap<String, String>();
        input.put("param1", "a&b c");
        input.put("param2", "a\r\nb");

        /*
         * & is escaped, \r and \n are escaped, parameters are divided by &
         */
        String str = persister.serialize(input);
        assertThat(str, is("param1=a\\u0026b c&param2=a\\r\\nb"));

        Map<String, String> output = persister.deserialize(str);
        assertEquals(input, output);
    }

}
