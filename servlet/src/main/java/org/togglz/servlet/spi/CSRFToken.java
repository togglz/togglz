package org.togglz.servlet.spi;

/**
 * A CSRF token
 */
public class CSRFToken {

    private final String name;

    private final String value;

    public CSRFToken(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

}
