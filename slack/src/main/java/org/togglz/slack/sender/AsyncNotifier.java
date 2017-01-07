package org.togglz.slack.sender;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.togglz.slack.notification.Notification;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncNotifier implements NotificationSender {

    private static final String NAME_FORMAT = "togglz-slack-%d";

    private final NotificationSender delegate;

    private final Executor executor;

    public AsyncNotifier(String slackHookUrl) {
        this(slackHookUrl, singleThreadExecutor());
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

    private static ExecutorService singleThreadExecutor() {
        return Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder()
                        .setNameFormat(NAME_FORMAT)
                        .build());
    }
}
