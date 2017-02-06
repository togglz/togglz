package org.togglz.slack;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.SingleUserProvider;
import org.togglz.core.user.UserProvider;
import org.togglz.slack.config.NotificationConfiguration;

/**
 * @author Tomasz Skowroński
 * @since 2.4.0
 */
public class SlackStateRepository implements StateRepository {

    private final StateRepository wrappedRepository;
    private final SlackNotifications slackNotifications;

    /**
     * Main constructor. For more information see /slack/README.md.
     */
    public SlackStateRepository(StateRepository wrappedRepository, NotificationConfiguration configuration, UserProvider userProvider) {
        this(wrappedRepository, new SlackNotifications(configuration, userProvider));
    }

    public SlackStateRepository(StateRepository wrappedRepository, NotificationConfiguration configuration) {
        this(wrappedRepository, new SlackNotifications(configuration, new SingleUserProvider("unknown")));
    }

    public SlackStateRepository(StateRepository wrappedRepository, SlackNotifications slackNotifications) {
        this.wrappedRepository = wrappedRepository;
        this.slackNotifications = slackNotifications;
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        return wrappedRepository.getFeatureState(feature);
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        wrappedRepository.setFeatureState(featureState);
        slackNotifications.notify(featureState);
    }
}
