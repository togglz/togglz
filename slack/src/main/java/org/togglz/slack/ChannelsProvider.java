package org.togglz.slack;

import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.util.Preconditions;

import java.util.LinkedList;
import java.util.List;

class ChannelsProvider {

    private static final Log log = LogFactory.getLog(ChannelsProvider.class);

    private final List<String> channels;

    ChannelsProvider(List<String> channels) {
        Preconditions.checkArgument(channels != null, "channels can be empty but not null");
        this.channels = new LinkedList<>(channels);
        log.info("Slack toggles channels: " + channels);
    }

    List<String> getRecipients() {
        return channels;
    }
}
