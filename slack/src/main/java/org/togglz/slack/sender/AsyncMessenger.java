package org.togglz.slack.sender;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.togglz.slack.message.Message;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncMessenger implements MessageSender {

    private static final String NAME_FORMAT = "togglz-slack-%d";

    private final MessageSender delegate;

    private final Executor executor;

    public AsyncMessenger(String slackHookUrl) {
        this(slackHookUrl, singleThreadExecutor());
    }

    public AsyncMessenger(String slackHookUrl, Executor executor) {
        this.delegate = new Messenger(slackHookUrl);
        this.executor = executor;
    }

    @Override
    public void send(final Message message) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                delegate.send(message);
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
