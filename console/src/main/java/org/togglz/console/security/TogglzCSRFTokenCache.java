package org.togglz.console.security;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.togglz.servlet.spi.CSRFToken;

public class TogglzCSRFTokenCache {

	private static final PassiveExpiringMap<String, CSRFToken> expiringMap;
	private static final Object lock = new Object();

	static {
		PassiveExpiringMap.ConstantTimeToLiveExpirationPolicy<String, CSRFToken>
				expirationPolicy = new PassiveExpiringMap.ConstantTimeToLiveExpirationPolicy<>(
				10, TimeUnit.MINUTES);
		expiringMap = new PassiveExpiringMap<>(expirationPolicy, new HashMap<>());
	}

	static void cacheToken(CSRFToken token) {
		synchronized (lock) {
			expiringMap.put(token.getValue(), token);
		}
	}

	static boolean isTokenInCache(CSRFToken token) {
		synchronized (lock) {
			return expiringMap.containsKey(token.getValue());
		}
	}
	
}