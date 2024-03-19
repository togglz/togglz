package org.togglz.console.security;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.togglz.servlet.spi.CSRFToken;

public class TogglzCSRFTokenCache {

	private static final PassiveExpiringMap<String, CSRFToken> expiringMap;
    private static final ReentrantLock lock = new ReentrantLock();

	static {
		PassiveExpiringMap.ConstantTimeToLiveExpirationPolicy<String, CSRFToken>
				expirationPolicy = new PassiveExpiringMap.ConstantTimeToLiveExpirationPolicy<>(
				10, TimeUnit.MINUTES);
		expiringMap = new PassiveExpiringMap<>(expirationPolicy, new HashMap<>());
	}

	static void cacheToken(CSRFToken token) {
        lock.lock();
        try {
            expiringMap.put(token.getValue(), token);
        } finally {
            lock.unlock();
        }
	}

	static boolean isTokenInCache(CSRFToken token) {
        lock.lock();
		try {
			return expiringMap.containsKey(token.getValue());
		} finally {
            lock.unlock();
        }
	}
}
