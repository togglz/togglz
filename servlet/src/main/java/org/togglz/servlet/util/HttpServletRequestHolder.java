package org.togglz.servlet.util;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * This class can be used to bind the {@link HttpServletRequest} to a thread local. Please take special care to ALWAYS remove
 * the request from the thread local by calling {@link #release()}.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class HttpServletRequestHolder {

    private static ThreadLocal<HttpServletRequest> threadLocal = new ThreadLocal<HttpServletRequest>();

    /**
     * Associate the request with the current thread.
     */
    public static void bind(HttpServletRequest request) {
        if (request != null && threadLocal.get() != null) {
            throw new IllegalStateException("HttpServletRequestHolder.bind() called for a "
                    + "thread that already has a request associated with it. It's likely that the request "
                    + "was not correctly removed from the thread before it is put back into the thread pool.");
        }
        threadLocal.set(request);
    }

    /**
     * Remove the request that is currently associated with the current thread.
     */
    public static void release() {
        threadLocal.set(null);
    }

    public static HttpServletRequest get() {
        return threadLocal.get();
    }

}
