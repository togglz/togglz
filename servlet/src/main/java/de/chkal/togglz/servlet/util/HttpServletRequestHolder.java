package de.chkal.togglz.servlet.util;

import javax.servlet.http.HttpServletRequest;

public class HttpServletRequestHolder {

    private static ThreadLocal<HttpServletRequest> threadLocal = new ThreadLocal<HttpServletRequest>();

    public static void set(HttpServletRequest request) {
        if (request != null && threadLocal.get() != null) {
            throw new IllegalStateException("HttpServletRequestHolder.set() called for a "
                    + "thread that already has a request associated with it. It's likely that the request "
                    + "was not correctly removed from the thread before it is put back into the thread pool.");
        }
        threadLocal.set(request);
    }

    public static HttpServletRequest get() {
        return threadLocal.get();
    }

}
