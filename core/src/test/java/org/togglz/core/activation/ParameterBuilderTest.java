package org.togglz.core.activation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ParameterBuilderTest {

    @Test
    public void testMinimalParameter() {

        Parameter param = ParameterBuilder.create("uniqueId");

        assertEquals(param.getId(), "uniqueId");
        assertEquals(param.getName(), "uniqueId");
        assertTrue(param.isValid("everything will match"));

    }

    @Test
    public void testParameterWithCustomName() {

        Parameter param = ParameterBuilder.create("uniqueId").named("My Parameter");

        assertEquals(param.getId(), "uniqueId");
        assertEquals(param.getName(), "My Parameter");
        assertTrue(param.isValid("everything will match"));

    }

    @Test
    public void testParameterWithRegularExpression() {

        Parameter param = ParameterBuilder.create("uniqueId").matching("[a-z]+");

        assertEquals(param.getId(), "uniqueId");
        assertEquals(param.getName(), "uniqueId");

        assertTrue(param.isValid("lowercase"));
        assertFalse(param.isValid("UPPERCASE"));

    }

}
