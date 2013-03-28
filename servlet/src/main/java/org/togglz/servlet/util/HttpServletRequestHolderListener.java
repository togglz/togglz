package org.togglz.servlet.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.togglz.servlet.spi.RequestListener;

/**
 * 
 * Implementation of {@link RequestListener} that is responsible for binding and releasing the current request for
 * {@link HttpServletRequestHolder}.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class HttpServletRequestHolderListener implements RequestListener {

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public void begin(HttpServletRequest request, HttpServletResponse response) {
        HttpServletRequestHolder.bind(request);
    }

    @Override
    public void end(HttpServletRequest request, HttpServletResponse response) {
        HttpServletRequestHolder.release();
    }

}
