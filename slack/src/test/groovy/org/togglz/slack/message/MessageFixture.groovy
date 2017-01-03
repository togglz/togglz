package org.togglz.slack.message

class MessageFixture {

    private static final String DEFAULT_ICON = EmojiIcon.valueOf("robot_face").format()

    static Message create() {
        Message message = new Message();
        message.setChannel("toggles");
        message.setUsername("togglz.slack");
        message.setText("test message");
        message.setIcon(DEFAULT_ICON);
        message.setMarkdown(true);
        return message
    }

    static String createAsJson() {
        return '{"channel":"toggles","username":"togglz.slack","text":"test message","icon_emoji":":robot_face:","mrkdwn":true}'
    }
}
