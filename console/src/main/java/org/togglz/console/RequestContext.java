package org.togglz.console;

public class RequestContext {

    private final boolean validateCSRFToken;

    public RequestContext(boolean validateCSRFToken) {
        this.validateCSRFToken = validateCSRFToken;
    }

    public boolean isValidateCSRFToken() {
        return validateCSRFToken;
    }
}
