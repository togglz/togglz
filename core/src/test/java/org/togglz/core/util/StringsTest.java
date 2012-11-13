package org.togglz.core.util;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class StringsTest {

    @Test
    public void splitAndTrim() {

        assertEquals(0, Strings.splitAndTrim(null, ",").size());
        assertEquals(0, Strings.splitAndTrim("   ", ",").size());

        List<String> first = Strings.splitAndTrim("   ,foo", ",");
        assertEquals(1, first.size());
        assertEquals("foo", first.get(0));

        List<String> second = Strings.splitAndTrim(" foo,,,bar  ", ",");
        assertEquals(2, second.size());
        assertEquals("foo", second.get(0));
        assertEquals("bar", second.get(1));

    }

}
