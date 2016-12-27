package org.togglz.slack.sender;

import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.slack.message.Message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

/**
 * For documentation see https://api.slack.com/incoming-webhooks
 */
public class Messenger implements MessageSender {

    private static final Log log = LogFactory.getLog(Messenger.class);

    private final HttpPostRequest httpPostRequest;

    private final ObjectMapper mapper;

    public Messenger(String slackHookUrl) {
        this.httpPostRequest = new HttpPostRequest(slackHookUrl);
        this.mapper = new ObjectMapper();
    }

    @Override
    public void send(Message message) {
        String json = toJson(message);
        if (json != null) {
            String response = httpPostRequest.send(json);
            if (!Strings.isNullOrEmpty(response)) {
                log.debug(response);
            }
        }
    }

    private String toJson(Message message) {
        try {
            return mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.error(e.toString(), e);
            return null;
        }
    }
}
