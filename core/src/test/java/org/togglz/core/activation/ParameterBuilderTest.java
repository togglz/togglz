package org.togglz.core.activation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParameterBuilderTest {

    @Test
    void testMinimalParameter() {
        Parameter param = ParameterBuilder.create("uniqueId");

        assertEquals(param.getName(), "uniqueId");
        assertEquals(param.getLabel(), "uniqueId");
        assertTrue(param.isValid("everything will match"));
        assertFalse(param.isOptional());
        assertFalse(param.isLargeText());
        assertNull(param.getDescription());
    }

    @Test
    void testParameterWithCustomName() {
        Parameter param = ParameterBuilder.create("uniqueId").label("My Parameter");

        assertEquals(param.getName(), "uniqueId");
        assertEquals(param.getLabel(), "My Parameter");
        assertTrue(param.isValid("everything will match"));
        assertFalse(param.isOptional());
        assertFalse(param.isLargeText());
        assertNull(param.getDescription());
    }

    @Test
    void testOptionalParameter() {
        Parameter param = ParameterBuilder.create("uniqueId").label("My Parameter").optional();

        assertEquals(param.getName(), "uniqueId");
        assertEquals(param.getLabel(), "My Parameter");
        assertTrue(param.isValid("everything will match"));
        assertTrue(param.isOptional());
        assertFalse(param.isLargeText());
        assertNull(param.getDescription());
    }

    @Test
    void testLargeTextParameter() {
        Parameter param = ParameterBuilder.create("uniqueId").label("My Parameter").largeText();

        assertEquals(param.getName(), "uniqueId");
        assertEquals(param.getLabel(), "My Parameter");
        assertTrue(param.isValid("everything will match"));
        assertFalse(param.isOptional());
        assertTrue(param.isLargeText());
        assertNull(param.getDescription());
    }

    @Test
    void testParameterWithDescription() {
        Parameter param = ParameterBuilder.create("uniqueId").label("My Parameter").description("Some text");

        assertEquals(param.getName(), "uniqueId");
        assertEquals(param.getLabel(), "My Parameter");
        assertTrue(param.isValid("everything will match"));
        assertFalse(param.isOptional());
        assertFalse(param.isLargeText());
        assertEquals(param.getDescription(), "Some text");
    }

    @Test
    void testParameterWithRegularExpression() {
        Parameter param = ParameterBuilder.create("uniqueId").matching("[a-z]+");

        assertEquals(param.getName(), "uniqueId");
        assertEquals(param.getLabel(), "uniqueId");
        assertFalse(param.isOptional());
        assertFalse(param.isLargeText());
        assertNull(param.getDescription());

        assertTrue(param.isValid("lowercase"));
        assertFalse(param.isValid("UPPERCASE"));
    }
}
