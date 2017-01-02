Slack notifications
-------------------

[Slack](https://slack.com) is real-time messaging, archiving and search for teams.
 This togglz-slack module allows to notify your team about toggles changes.
 
1. Set up an [incoming webhook integration](https://my.slack.com/services/new/incoming-webhook/) in your Slack team.
2. Copy your own webhook URL from Slack website.
3. Add togglz-slack to your project dependencies and configure it.
 
## SlackStateRepository 

SlackStateRepository must be secondary StateRepository, cannot be the only StateRepository.
This is because Slack repository is for notifications so it is write-only.
To simplify configuration you can use SlackWrapperStateRepository as composite repository to wrap your previous StateRepository. 

## Example Spring configuration
 
```
@Configuration
public class TogglzSlackConfig {  
    
    @Bean
    public NotificationConfiguration notificationConfiguration(@Value("${togglz.slack.hookUrl}" hookUrl){
        return NotificationConfiguration.builder()
            .withSlackHookUrl(hookUrl)
            // more (optional) notification configuration here
            .build();
    }

    @Bean
    public SlackWrapperStateRepository stateRepository(StateRepository wrapped, NotificationConfiguration config, UserProvider users) {
        SlackStateRepository notifications = new SlackStateRepository(config, users)
        return new SlackWrapperStateRepository(wrapped, notifications);
    }

}
 ```

You need to provide _wrapped_ bean (your old StateRepository) and _togglz.slack.hookUrl_ in properties.
Now you can use _SlackWrapperStateRepository_ to persist toggles state and to have Slack notifications at the same time.
Read more about [Java-based container configuration](https://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/#beans-java).