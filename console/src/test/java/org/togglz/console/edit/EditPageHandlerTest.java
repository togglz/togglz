package org.togglz.console.edit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.togglz.console.handlers.edit.EditPageHandler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditPageHandlerTest {

    private final EditPageHandler testee = new EditPageHandler();

    @Test
    void shouldHandleEditPath() {
        assertTrue(testee.handles("/edit"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"edit", "//edit", "/dit", "/edi"})
    void shouldNotHandleEditPaths(String path) {
        assertFalse(testee.handles(path));
    }

    @Test
    void shouldBeAdminOnly() {
        assertTrue(testee.adminOnly());
    }
}