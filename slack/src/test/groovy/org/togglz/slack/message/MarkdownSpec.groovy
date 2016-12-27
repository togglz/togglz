package org.togglz.slack.message

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class MarkdownSpec extends Specification {

    def "should format text"() {
        expect:
            style.format("text") == result
        where:
            style           | result
            Markdown.BOLD   | "*text*"
            Markdown.CODE   | "`text`"
            Markdown.ITALIC | "_text_"
            Markdown.STRIKE | "~text~"
            Markdown.PRE    | "```text```"
    }

    def "should format with empty text"() {
        expect:
            Markdown.BOLD.format(input) == output
        where:
            input | output
            null  | ""
            ""    | ""
            " "   | "* *"
    }

    def "should format link from '#url' and '#name'"() {
        expect:
            Markdown.link(url, name) == result
        where:
            url    | name  | result
            null   | null  | null
            "http" | "abc" | "<http|abc>"
            // no name cases
            "http" | " "   | "<http| >"
            "http" | ""    | "<http|http>"
            "http" | null  | "<http|http>"
            // no url cases
            " "    | "abc" | "abc"
            ""     | "abc" | "abc"
            null   | "abc" | "abc"
    }
}
