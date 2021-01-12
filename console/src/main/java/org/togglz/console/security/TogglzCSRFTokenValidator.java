package org.togglz.console.security;

import javax.servlet.http.HttpServletRequest;

import org.togglz.servlet.spi.CSRFToken;
import org.togglz.servlet.spi.CSRFTokenValidator;

public class TogglzCSRFTokenValidator implements CSRFTokenValidator {

	static final String CSRF_TOKEN_NAME = "togglz_csrf";

	@Override
	public boolean isTokenValid(HttpServletRequest request) {
		String token = request.getParameter(CSRF_TOKEN_NAME);
		if(token==null) {
			return false;
		} else {
			return TogglzCSRFTokenCache.isTokenInCache(new CSRFToken(CSRF_TOKEN_NAME,token));
		}
	}
}
