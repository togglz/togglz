package org.togglz.servlet.spi;

import jakarta.servlet.http.HttpServletRequest;

public interface CSRFTokenValidator {

	boolean isTokenValid(HttpServletRequest request);
}
