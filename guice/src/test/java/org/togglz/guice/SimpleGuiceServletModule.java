package org.togglz.guice;

import org.togglz.core.manager.TogglzConfig;

import com.google.inject.AbstractModule;

public class SimpleGuiceServletModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TogglzConfig.class).to(GuiceIntegrationConfig.class);
    }

}
