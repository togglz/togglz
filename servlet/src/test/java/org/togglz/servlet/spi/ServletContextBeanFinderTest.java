package org.togglz.servlet.spi;

import org.junit.jupiter.api.Test;

import javax.servlet.ServletContext;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class ServletContextBeanFinderTest {

    @Test
    void shouldReturnEmptyListWhenContextisNotServletContext() {
        ServletContextBeanFinder finder = new ServletContextBeanFinder();
        String context = "";
        Collection<String> strings = finder.find(String.class, context);
        assertEquals(0, strings.size());
    }

    @Test
    void shouldReturnEmptyListWhenParameterIsntInServletContext() {
        ServletContextBeanFinder finder = new ServletContextBeanFinder();
        ServletContext context = new MyServletContext();
        Collection<String> strings = finder.find(String.class, context);
        assertEquals(1, strings.size());
        assertEquals("", strings.stream().findFirst().get());
    }

    @Test
    void testCreateInstance() {
        CharSequence string = ServletContextBeanFinder.createInstance("java.lang.String", CharSequence.class);
        assertNotNull(string);
        assertTrue(string instanceof String);
        assertEquals("", string);
    }
}