package de.chkal.togglz.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ClassUtilsTest {

    @Test
    public void testCreateInstance() {
        CharSequence string = ClassUtils.createInstance("java.lang.String", CharSequence.class);
        assertNotNull(string);
        assertTrue(string instanceof String);
        assertEquals("", string);
    }

}
