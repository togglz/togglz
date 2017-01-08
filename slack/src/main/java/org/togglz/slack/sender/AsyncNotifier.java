package org.togglz.slack.sender;

import org.togglz.slack.notification.Notification;

import java.util.concurrent.Executor;

public class AsyncNotifier implements NotificationSender {

    private final NotificationSender delegate;

    private final Executor executor;

    public AsyncNotifier(String slackHookUrl) {
        this(slackHookUrl, ExecutorServiceFactory.createSingleThreadExecutor());
    }

    public AsyncNotifier(String slackHookUrl, Executor executor) {
        this.delegate = new Notifier(slackHookUrl);
        this.executor = executor;
    }

    @Override
    public void send(final Notification notification) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                delegate.send(notification);
            }
        });
    }
}
