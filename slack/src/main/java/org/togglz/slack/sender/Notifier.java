package org.togglz.slack.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.util.Strings;
import org.togglz.slack.notification.Notification;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * For documentation see https://api.slack.com/incoming-webhooks
 */
public class Notifier implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(Notifier.class);

    private final HttpPostRequest httpPostRequest;

    private final ObjectMapper mapper;

    public Notifier(String slackHookUrl) {
        this.httpPostRequest = new HttpPostRequest(slackHookUrl);
        this.mapper = new ObjectMapper();
    }

    @Override
    public void send(Notification notification) {
        byte[] json = toJsonAsBytes(notification);
        if (json != null) {
            String response = httpPostRequest.send(json);
            if (Strings.isNotBlank(response)) {
                log.debug(response);
            }
        }
    }

    private byte[] toJsonAsBytes(Notification notification) {
        try {
            return mapper.writeValueAsBytes(notification);
        } catch (JacksonException e) {
            log.error(e.toString(), e);
            return null;
        }
    }
}
