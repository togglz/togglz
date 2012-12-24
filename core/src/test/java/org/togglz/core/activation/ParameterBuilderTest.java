package org.togglz.core.activation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ParameterBuilderTest {

    @Test
    public void testMinimalParameter() {

        Parameter param = ParameterBuilder.create("uniqueId");

        assertEquals(param.getName(), "uniqueId");
        assertEquals(param.getLabel(), "uniqueId");
        assertTrue(param.isValid("everything will match"));
        assertFalse(param.isOptional());
        assertFalse(param.isLargeText());
        assertNull(param.getDescription());

    }

    @Test
    public void testParameterWithCustomName() {

        Parameter param = ParameterBuilder.create("uniqueId").label("My Parameter");

        assertEquals(param.getName(), "uniqueId");
        assertEquals(param.getLabel(), "My Parameter");
        assertTrue(param.isValid("everything will match"));
        assertFalse(param.isOptional());
        assertFalse(param.isLargeText());
        assertNull(param.getDescription());

    }

    @Test
    public void testOptionalParameter() {

        Parameter param = ParameterBuilder.create("uniqueId").label("My Parameter").optional();

        assertEquals(param.getName(), "uniqueId");
        assertEquals(param.getLabel(), "My Parameter");
        assertTrue(param.isValid("everything will match"));
        assertTrue(param.isOptional());
        assertFalse(param.isLargeText());
        assertNull(param.getDescription());

    }

    @Test
    public void testLargeTextParameter() {

        Parameter param = ParameterBuilder.create("uniqueId").label("My Parameter").largeText();

        assertEquals(param.getName(), "uniqueId");
        assertEquals(param.getLabel(), "My Parameter");
        assertTrue(param.isValid("everything will match"));
        assertFalse(param.isOptional());
        assertTrue(param.isLargeText());
        assertNull(param.getDescription());

    }

    @Test
    public void testParameterWithDescription() {

        Parameter param = ParameterBuilder.create("uniqueId").label("My Parameter").description("Some text");

        assertEquals(param.getName(), "uniqueId");
        assertEquals(param.getLabel(), "My Parameter");
        assertTrue(param.isValid("everything will match"));
        assertFalse(param.isOptional());
        assertFalse(param.isLargeText());
        assertEquals(param.getDescription(), "Some text");

    }

    @Test
    public void testParameterWithRegularExpression() {

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
