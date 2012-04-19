package org.togglz.test.jsf.map;

import javax.enterprise.context.ApplicationScoped;

import org.togglz.core.Feature;
import org.togglz.core.config.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.UserProvider;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.test.jsf.JSFFeatures;


@ApplicationScoped
public class JSFMapConfiguration implements TogglzConfig {

    @Override
    public Class<? extends Feature> getFeatureClass() {
        return JSFFeatures.class;
    }

    @Override
    public StateRepository getStateRepository() {
        return new InMemoryStateRepository();
    }

    @Override
    public UserProvider getUserProvider() {
        return new NoOpUserProvider();
    }

}
