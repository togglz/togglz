package org.togglz.slack;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;

import java.util.List;

class ChannelsProvider {

    private static final Log log = LogFactory.getLog(ChannelsProvider.class);

    private final List<String> channels;

    ChannelsProvider(List<String> channels) {
        Preconditions.checkArgument(channels != null, "channels can be empty but not null");
        this.channels = ImmutableList.copyOf(channels);
        log.info("Slack toggles channels: " + channels);
    }

    List<String> getRecipients() {
        return channels;
    }
}
