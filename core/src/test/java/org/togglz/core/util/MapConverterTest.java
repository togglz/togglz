package org.togglz.core.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class MapConverterTest {

    @Test
    public void testWithNewLines() {

        MapConverter persister = MapConverter.create().withNewLines();

        Map<String, String> input = new HashMap<String, String>();
        input.put("param1", "a&b c");
        input.put("param2", "a\r\nb");

        /*
         * & is not escaped, \r and \n are escaped, parameters are divided by \r\n
         */
        String str = persister.convertToString(input);
        assertThat(str, is("param1=a&b c\r\nparam2=a\\r\\nb\r\n"));

        Map<String, String> output = persister.convertFromString(str);
        assertEquals(input, output);

    }

    @Test
    public void testWithoutNewLines() {

        MapConverter persister = MapConverter.create().withoutNewLines();

        Map<String, String> input = new HashMap<String, String>();
        input.put("param1", "a&b c");
        input.put("param2", "a\r\nb");

        /*
         * & is escaped, \r and \n are escaped, parameters are divided by &
         */
        String str = persister.convertToString(input);
        assertThat(str, is("param1=a\\u0026b c&param2=a\\r\\nb"));

        Map<String, String> output = persister.convertFromString(str);
        assertEquals(input, output);
    }

}
