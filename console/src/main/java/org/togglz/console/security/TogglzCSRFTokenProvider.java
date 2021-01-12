package org.togglz.console.security;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.togglz.servlet.spi.CSRFToken;
import org.togglz.servlet.spi.CSRFTokenProvider;

import static org.togglz.console.security.TogglzCSRFTokenValidator.CSRF_TOKEN_NAME;

public class TogglzCSRFTokenProvider implements CSRFTokenProvider {

	@Override
	public CSRFToken getToken(HttpServletRequest request) {
		CSRFToken token;
		if (request.getAttribute(CSRF_TOKEN_NAME) == null) {
			token = new CSRFToken(CSRF_TOKEN_NAME, UUID.randomUUID().toString());
			TogglzCSRFTokenCache.cacheToken(token);
		} else {
			token = new CSRFToken(CSRF_TOKEN_NAME, request.getAttribute(CSRF_TOKEN_NAME).toString());
		}
		return token;
	}
}