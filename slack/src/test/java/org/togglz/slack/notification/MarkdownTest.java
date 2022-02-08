package org.togglz.slack.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MarkdownTest {

    @Test
    void shouldFormatText() {
        assertEquals("*text*", Markdown.BOLD.format("text"));
        assertEquals("`text`", Markdown.CODE.format("text"));
        assertEquals("_text_", Markdown.ITALIC.format("text"));
        assertEquals("~text~", Markdown.STRIKE.format("text"));
        assertEquals("```text```", Markdown.PRE.format("text"));
    }

    @Test
    void cannotFormatEmptyText() {
        assertEquals("", Markdown.BOLD.format(null));
        assertEquals("", Markdown.CODE.format(""));
        assertEquals("", Markdown.ITALIC.format(" "));
    }

    @Test
    void shouldFormatLinkFromUrlAndName() {
        Assertions.assertNull(Markdown.link(null, null));
        assertEquals("<http|abc>", Markdown.link("http", "abc"));
    }

    @Test
    void shouldFormatLinkFromUrlAndEmptyName() {
        assertEquals("<http|http>", Markdown.link("http", null));
        assertEquals("<http|http>", Markdown.link("http", ""));
        assertEquals("<http|http>", Markdown.link("http", " "));
    }

    @Test
    void cannotFormatLinkFromEmptyUrlAndName() {
        assertEquals("abc", Markdown.link(null, "abc"));
        assertEquals("abc", Markdown.link("", "abc"));
        assertEquals("abc", Markdown.link(" ", "abc"));
    }
}
