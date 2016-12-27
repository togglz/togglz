package org.togglz.slack;

import java.util.List;

import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;

import com.google.common.base.Preconditions;

class ChannelsProvider {

    private static final Log log = LogFactory.getLog(ChannelsProvider.class);

    private final List<String> channels;

    ChannelsProvider(List<String> channels) {
        Preconditions.checkArgument(channels != null, "channels can be empty but not null");
        this.channels = channels;
        log.info("Slack toggles channels: " + this.channels);
    }

    public List<String> getRecipients() {
        return channels;
    }
}
