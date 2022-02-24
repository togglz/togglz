package org.togglz.servlet;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.util.Validate;
import org.togglz.servlet.spi.RequestListener;

/**
 *
 * This class is used by {@link TogglzFilter} to notify {@link RequestListener} implementation about requests. This class tries
 * to ensure that all listeners will receive the notifies, even if one of the listeners fails with a {@link RuntimeException}.
 *
 * @author Christian Kaltepoth
 *
 */
class CompositeRequestListener {

    private final Logger log = LoggerFactory.getLogger(CompositeRequestListener.class);

    private final List<RequestListener> listeners;

    public CompositeRequestListener(List<RequestListener> listeners) {
        Validate.notNull(listeners, "No listeners");
        this.listeners = new ArrayList<RequestListener>(listeners);
    }

    public void begin(HttpServletRequest request, HttpServletResponse response) {
        for (RequestListener listener : listeners) {
            try {
                listener.begin(request, response);
            } catch (Exception e) {
                log.error("Failed to execute RequestListener", e);
            }
        }
    }

    public void end(HttpServletRequest request, HttpServletResponse response) {
        for (RequestListener listener : listeners) {
            try {
                listener.end(request, response);
            } catch (Exception e) {
                log.error("Failed to execute RequestListener", e);
            }
        }
    }

}
