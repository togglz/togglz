package org.togglz.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClassUtilsTest {

    @Test
    void testCreateInstance() {
        CharSequence string = ClassUtils.createInstance("java.lang.String", CharSequence.class);
        assertNotNull(string);
        assertTrue(string instanceof String);
        assertEquals("", string);
    }
}
