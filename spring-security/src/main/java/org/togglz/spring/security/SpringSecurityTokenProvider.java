package org.togglz.spring.security;

import org.springframework.security.web.csrf.CsrfToken;
import org.togglz.servlet.spi.CSRFToken;
import org.togglz.servlet.spi.CSRFTokenProvider;

import javax.servlet.http.HttpServletRequest;

/**
 * Implementation of CSRFTokenProvider for Spring Security.
 *
 * @see org.togglz.servlet.spi.CSRFTokenProvider
 */
public class SpringSecurityTokenProvider implements CSRFTokenProvider {

    @Override
    public CSRFToken getToken(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        if (token != null) {
            return new CSRFToken(token.getParameterName(), token.getToken());
        }
        return null;
    }

}
