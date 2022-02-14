package org.togglz.slack.notification;

public class NotificationFixture {

    public static Notification exampleNotification() {
        Notification notification = new Notification();
        notification.setChannel("toggles");
        notification.setUsername("togglz.slack");
        notification.setText("test notification");
        notification.setIcon(EmojiIcon.format("robot_face"));
        notification.setMarkdown(true);
        return notification;
    }

    public static String exampleNotificationAsJson() {
        return "{\"channel\":\"toggles\",\"username\":\"togglz.slack\",\"text\":\"test notification\",\"icon_emoji\":\":robot_face:\",\"mrkdwn\":true}";
    }
}
