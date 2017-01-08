package org.togglz.slack.sender;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

final class ExecutorServiceFactory {

    private static final String THREAD_NAME_FORMAT = "togglz-slack-%d";

    static ExecutorService createSingleThreadExecutor() {
        return Executors.newSingleThreadExecutor(createThreadFactory());
    }

    private static ThreadFactory createThreadFactory() {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setName(THREAD_NAME_FORMAT);
                return thread;
            }
        };
    }
}
