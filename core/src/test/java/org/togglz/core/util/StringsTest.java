package org.togglz.core.util;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class StringsTest {

    @Test
    public void testIsEmpty() {
        assertTrue(Strings.isEmpty(null));
        assertTrue(Strings.isEmpty(""));
        assertFalse(Strings.isEmpty("   "));
        assertFalse(Strings.isEmpty("foo"));
    }

    @Test
    public void testIsNotEmpty() {
        assertFalse(Strings.isNotEmpty(null));
        assertFalse(Strings.isNotEmpty(""));
        assertTrue(Strings.isNotEmpty("   "));
        assertTrue(Strings.isNotEmpty("foo"));
    }

    @Test
    public void testSplitAndTrim() {

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

    @Test
    public void testTrim() {
        assertNull(Strings.trim(null));
        assertEquals("", Strings.trim(""));
        assertEquals("", Strings.trim("   "));
        assertEquals("foo", Strings.trim("   foo   "));
    }

    @Test
    public void testTrimToNull() {
        assertNull(Strings.trimToNull(null));
        assertNull(Strings.trimToNull(""));
        assertNull(Strings.trimToNull("   "));
        assertEquals("foo", Strings.trimToNull("   foo   "));
    }

    @Test
    public void testToBoolean() {
        assertNull(Strings.toBoolean(null));
        assertNull(Strings.toBoolean(""));
        assertNull(Strings.toBoolean("   "));

        assertEquals(Boolean.TRUE, Strings.toBoolean("true"));
        assertEquals(Boolean.TRUE, Strings.toBoolean("   TRUE   "));
        assertEquals(Boolean.TRUE, Strings.toBoolean("on"));
        assertEquals(Boolean.TRUE, Strings.toBoolean("   ON   "));
        assertEquals(Boolean.TRUE, Strings.toBoolean("yes"));
        assertEquals(Boolean.TRUE, Strings.toBoolean("   YES   "));
        assertEquals(Boolean.TRUE, Strings.toBoolean("1"));
        assertEquals(Boolean.TRUE, Strings.toBoolean("   1   "));

        assertEquals(Boolean.FALSE, Strings.toBoolean("false"));
        assertEquals(Boolean.FALSE, Strings.toBoolean("   FALSE   "));
        assertEquals(Boolean.FALSE, Strings.toBoolean("off"));
        assertEquals(Boolean.FALSE, Strings.toBoolean("   OFF   "));
        assertEquals(Boolean.FALSE, Strings.toBoolean("no"));
        assertEquals(Boolean.FALSE, Strings.toBoolean("   NO   "));
        assertEquals(Boolean.FALSE, Strings.toBoolean("0"));
        assertEquals(Boolean.FALSE, Strings.toBoolean("   0   "));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToBooleanThrowsIfStringIsInvalid() {
        Strings.toBoolean("foo");
    }
}
