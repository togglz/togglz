package org.togglz.slack.notification

class NotificationFixture {

    static Notification exampleNotification() {
        Notification notification = new Notification();
        notification.setChannel("toggles");
        notification.setUsername("togglz.slack");
        notification.setText("test notification");
        notification.setIcon(EmojiIcon.format("robot_face"));
        notification.setMarkdown(true);
        return notification
    }

    static String exampleNotificationAsJson() {
        return '{"channel":"toggles","username":"togglz.slack","text":"test notification","icon_emoji":":robot_face:","mrkdwn":true}'
    }
}
