package org.togglz.console.handlers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ResourceHandlerTest {

    private final ResourceHandler testee = new ResourceHandler();

    @Test
    void shouldNotBeAdminOnly() {
        assertFalse(testee.adminOnly());
    }
}