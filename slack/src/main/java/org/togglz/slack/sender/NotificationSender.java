package org.togglz.slack.sender;

import org.togglz.slack.notification.Notification;

public interface NotificationSender {

    void send(Notification notification);
}
