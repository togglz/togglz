package org.togglz.jsp;

import org.togglz.core.Feature;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.UserProvider;

public class JspTaglibConfiguration implements TogglzConfig {

    @Override
    public Class<? extends Feature> getFeatureClass() {
        return JspTaglibFeature.class;
    }

    @Override
    public StateRepository getStateRepository() {
        InMemoryStateRepository repository = new InMemoryStateRepository();
        repository.setFeatureState(new FeatureState(JspTaglibFeature.ACTIVE_FEATURE, true));
        repository.setFeatureState(new FeatureState(JspTaglibFeature.INACTIVE_FEATURE, false));
        return repository;
    }

    @Override
    public UserProvider getUserProvider() {
        return new NoOpUserProvider();
    }

}
