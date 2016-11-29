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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.togglz.spring.util.ContextClassLoaderApplicationContextHolder;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link TogglzApplicationContextBinderApplicationListener}.
 *
 * @author Marcel Overdijk
 */
public class TogglzApplicationContextBinderApplicationListenerTest {

    private TogglzApplicationContextBinderApplicationListener applicationListener;
    private ApplicationContext applicationContext;

    @Before
    public void setUp() {
        applicationListener = new TogglzApplicationContextBinderApplicationListener();
        applicationContext = mock(ApplicationContext.class);
    }

    @After
    public void tearDown() {
        ContextClassLoaderApplicationContextHolder.release();
    }

    @Test
    public void contextRefreshed() {
        ContextRefreshedEvent contextRefreshedEvent = mock(ContextRefreshedEvent.class);
        when(contextRefreshedEvent.getApplicationContext()).thenReturn(applicationContext);
        // Invoke context refreshed event
        applicationListener.onApplicationEvent(contextRefreshedEvent);
        // Assert application context bound
        assertSame(applicationContext, ContextClassLoaderApplicationContextHolder.get());
    }

    @Test
    public void contextRefreshedWhileContextAlreadyBound() {
        // Bind application context before context refreshed event invoked
        ContextClassLoaderApplicationContextHolder.bind(mock(ApplicationContext.class));
        applicationContext = mock(ApplicationContext.class);
        ContextRefreshedEvent contextRefreshedEvent = mock(ContextRefreshedEvent.class);
        when(contextRefreshedEvent.getApplicationContext()).thenReturn(applicationContext);
        // Invoke context refreshed application event
        applicationListener.onApplicationEvent(contextRefreshedEvent);
        // Assert application context bound
        assertSame(applicationContext, ContextClassLoaderApplicationContextHolder.get());
    }

    @Test
    public void contextClosed() {
        // Bind application context before context closed event invoked
        ContextClassLoaderApplicationContextHolder.bind(applicationContext);
        ContextClosedEvent contextClosedEvent = mock(ContextClosedEvent.class);
        // Invoke context closed event
        applicationListener.onApplicationEvent(contextClosedEvent);
        // Assert application context released
        assertNull(ContextClassLoaderApplicationContextHolder.get());
    }
}
