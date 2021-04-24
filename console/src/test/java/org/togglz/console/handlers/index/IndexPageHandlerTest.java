package org.togglz.console.handlers.index;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndexPageHandlerTest {

    private final IndexPageHandler testee = new IndexPageHandler();

    @Test
    void shouldHandleEditPath() {
        assertTrue(testee.handles("/index"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"index", "//index", "/ndex", "/ind"})
    void shouldNotHandleEditPaths(String path) {
        assertFalse(testee.handles(path));
    }

    @Test
    void shouldBeAdminOnly() {
        assertTrue(testee.adminOnly());
    }
}