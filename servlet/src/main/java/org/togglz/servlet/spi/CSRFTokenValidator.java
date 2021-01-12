package org.togglz.servlet.spi;

import javax.servlet.http.HttpServletRequest;

public interface CSRFTokenValidator {

	boolean isTokenValid(HttpServletRequest request);
}
