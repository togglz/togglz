package org.togglz.slack.notification

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

    def "cannot format empty text"() {
        expect:
            Markdown.BOLD.format(input) == ""
        where:
            input << [null, "", " "]
    }

    def "should format link from '#url' and '#name'"() {
        expect:
            Markdown.link(url, name) == result
        where:
            url    | name  | result
            null   | null  | null
            "http" | "abc" | "<http|abc>"
    }

    def "should format link from '#url' and empty '#name'"() {
        expect:
            Markdown.link("http", name) == "<http|http>"
        where:
            name << [null, "", " "]

    }

    def "cannot format link from empty '#url' and '#name'"() {
        expect:
            Markdown.link(url, "abc") == "abc"
        where:
            url << [null, "", " "]

    }
}
