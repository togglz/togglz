# Slack notifications

[Slack](https://slack.com) is real-time messaging, archiving and search for teams.
 This togglz-slack module allows to notify your team about feature toggles changes.

1. Set up an [incoming webhook integration](https://my.slack.com/services/new/incoming-webhook/) in your Slack team.
2. Copy your own webhook URL from Slack website.
3. Add `togglz-slack` artifact to your project dependencies and configure it.

## SlackStateRepository

`SlackStateRepository` wrap your existing `StateRepository`adding notifications on `setFeatureState`.

### Example Spring configuration

```
@Configuration
public class TogglzSlackConfig {

    @Bean
    public NotificationConfiguration notificationConfiguration(@Value("${togglz.slack.hookUrl}" hookUrl){
        return NotificationConfiguration.builder()
            .withSlackHookUrl(hookUrl)
            // more (optional) configuration here
            .build();
    }

    @Bean
    public SlackStateRepository slackStateRepository(StateRepository wrapped, NotificationConfiguration config, UserProvider users) {
        return new SlackStateRepository(wrapped, config, users)
    }

}
 ```

You need to provide _wrapped_ bean (your old `StateRepository`) and _togglz.slack.hookUrl_ in properties.
Now you can use _SlackStateRepository_ to persist toggles state and to have Slack notifications at the same time.
Read more about [Java-based container configuration](https://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/#beans-java).

### NotificationConfigurationBuilder

[Example configuration](src/test/groovy/org/togglz/slack/NotificationConfigurationFixture.groovy)
