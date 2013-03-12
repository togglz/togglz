package org.togglz.core.manager;

import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.metadata.EmptyFeatureMetaData;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.UserProvider;
import org.togglz.core.util.Lists;
import org.togglz.core.util.Validate;

/**
 * Default implementation of {@link FeatureManager}
 * 
 * @author Christian Kaltepoth
 * 
 */
public class DefaultFeatureManager implements FeatureManager {

    private final StateRepository stateRepository;
    private final UserProvider userProvider;
    private final List<ActivationStrategy> strategies;
    private final FeatureProvider featureProvider;

    DefaultFeatureManager(FeatureProvider featureProvider, StateRepository stateRepository, UserProvider userProvider) {
        this.featureProvider = featureProvider;
        this.stateRepository = stateRepository;
        this.userProvider = userProvider;
        this.strategies = Lists.asList(ServiceLoader.load(ActivationStrategy.class).iterator());
        Validate.notEmpty(strategies, "No ActivationStrategy implementations found");
    }

    public Set<Feature> getFeatures() {
        return Collections.unmodifiableSet(featureProvider.getFeatures());
    }

    @Override
    public FeatureMetaData getMetaData(Feature feature) {
        FeatureMetaData metadata = featureProvider.getMetaData(feature);
        if (metadata != null) {
            return metadata;
        }
        return new EmptyFeatureMetaData(feature);
    }

    public boolean isActive(Feature feature) {

        FeatureState state = stateRepository.getFeatureState(feature);

        if (state == null) {
            return getMetaData(feature).isEnabledByDefault();
        }

        if (state.isEnabled()) {

            // if no strategy is selected, the decision is simple
            String strategyId = state.getStrategyId();
            if (strategyId == null) {
                return true;
            }

            FeatureUser user = userProvider.getCurrentUser();

            // check the selected strategy
            for (ActivationStrategy strategy : strategies) {
                if (strategy.getId().equalsIgnoreCase(strategyId)) {
                    return strategy.isActive(state, user);
                }
            }
        }

        // if the strategy was not found, the feature should be off
        return false;

    }

    public FeatureState getFeatureState(Feature feature) {
        FeatureState state = stateRepository.getFeatureState(feature);
        if (state == null) {
            boolean enabled = getMetaData(feature).isEnabledByDefault();
            state = new FeatureState(feature, enabled);
        }
        return state;
    }

    public void setFeatureState(FeatureState state) {
        stateRepository.setFeatureState(state);
    }

    @Override
    public FeatureUser getCurrentFeatureUser() {
        return userProvider.getCurrentUser();
    }

}
