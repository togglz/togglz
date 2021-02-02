package org.togglz.console;

public class RequestContext {

    private final boolean validateCSRFToken;

    private RequestContext(Builder builder) {
        validateCSRFToken = builder.validateCSRFToken;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public boolean isValidateCSRFToken() {
        return validateCSRFToken;
    }

    public static final class Builder {
        private boolean validateCSRFToken;

        private Builder() {
        }

        public Builder withValidateCSRFToken(boolean val) {
            validateCSRFToken = val;
            return this;
        }

        public RequestContext build() {
            return new RequestContext(this);
        }
    }
}
