package org.togglz.slack.sender;

import org.togglz.slack.message.Message;

public interface MessageSender {

    void send(Message message);
}
