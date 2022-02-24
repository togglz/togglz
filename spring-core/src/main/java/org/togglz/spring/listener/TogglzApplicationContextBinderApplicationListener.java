/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.togglz.spring.listener;

import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.togglz.spring.util.ContextClassLoaderApplicationContextHolder;

/**
 * {@link ApplicationListener} that binds the {@link ApplicationContext}
 * to the Togglz {@link ContextClassLoaderApplicationContextHolder}.
 *
 * @author Marcel Overdijk
 */
public class TogglzApplicationContextBinderApplicationListener implements ApplicationListener {

    private static final Logger log = LoggerFactory.getLogger(TogglzApplicationContextBinderApplicationListener.class);

    private static final ContextRefreshedEventFilter ACCEPT_ALL = t -> true;

    private final ContextRefreshedEventFilter filter;

    public TogglzApplicationContextBinderApplicationListener() {
        this(null);
    }

    public TogglzApplicationContextBinderApplicationListener(ContextRefreshedEventFilter filter) {
        this.filter = (filter == null ? ACCEPT_ALL : filter);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent && filter.test((ContextRefreshedEvent) event)) {
            if (ContextClassLoaderApplicationContextHolder.get() != null) {
                log.warn("ApplicationContext already bound to current context class loader, releasing it first");
                ContextClassLoaderApplicationContextHolder.release();
            }
            ApplicationContext applicationContext = ((ContextRefreshedEvent) event).getApplicationContext();
            ContextClassLoaderApplicationContextHolder.bind(applicationContext);
        } else if (event instanceof ContextClosedEvent) {
            ContextClassLoaderApplicationContextHolder.release();
        }
    }

    /**
     * A filter that can be used to exclude {@link ContextRefreshedEvent} events used to populate {@link ContextClassLoaderApplicationContextHolder}.
     *
     * @see <a href="https://github.com/togglz/togglz/issues/279">Issue 279</a>
     */
    @FunctionalInterface
    public interface ContextRefreshedEventFilter extends Predicate<ContextRefreshedEvent> {

        boolean test(ContextRefreshedEvent t);
    }
}
