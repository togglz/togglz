package org.togglz.slack;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.util.Preconditions;

class ChannelsProvider {

    private static final Logger log = LoggerFactory.getLogger(ChannelsProvider.class);

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
