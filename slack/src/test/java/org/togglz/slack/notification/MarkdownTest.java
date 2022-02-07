package org.togglz.slack.notification;

import org.junit.Test;

import static org.junit.Assert.*;

public class MarkdownTest {

    @Test
    public void shouldFormatText() {
        assertEquals("*text*", Markdown.BOLD.format("text"));
        assertEquals("`text`", Markdown.CODE.format("text"));
        assertEquals("_text_", Markdown.ITALIC.format("text"));
        assertEquals("~text~", Markdown.STRIKE.format("text"));
        assertEquals("```text```", Markdown.PRE.format("text"));
    }

    @Test
    public void cannotFormatEmptyText() {
        assertEquals("", Markdown.BOLD.format(null));
        assertEquals("", Markdown.CODE.format(""));
        assertEquals("", Markdown.ITALIC.format(" "));
    }

    @Test
    public void shouldFormatLinkFromUrlAndName() {
        assertNull(Markdown.link(null, null));
        assertEquals("<http|abc>", Markdown.link("http", "abc"));
    }

    @Test
    public void shouldFormatLinkFromUrlAndEmptyName() {
        assertEquals("<http|http>", Markdown.link("http", null));
        assertEquals("<http|http>", Markdown.link("http", ""));
        assertEquals("<http|http>", Markdown.link("http", " "));
    }

    @Test
    public void cannotFormatLinkFromEmptyUrlAndName() {
        assertEquals("abc", Markdown.link(null, "abc"));
        assertEquals("abc", Markdown.link("", "abc"));
        assertEquals("abc", Markdown.link(" ", "abc"));
    }
}
