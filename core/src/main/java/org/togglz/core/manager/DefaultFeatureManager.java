package org.togglz.core.manager;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.activation.ActivationStrategyProvider;
import org.togglz.core.metadata.EmptyFeatureMetaData;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.UserProvider;
import org.togglz.core.util.Validate;

/**
 * Default implementation of {@link FeatureManager}
 * 
 * @author Christian Kaltepoth
 * 
 */
public class DefaultFeatureManager implements FeatureManager {

    private final String name;
    private final StateRepository stateRepository;
    private final UserProvider userProvider;
    private final FeatureProvider featureProvider;
    private final ActivationStrategyProvider strategyProvider;

    DefaultFeatureManager(String name, FeatureProvider featureProvider, StateRepository stateRepository,
        UserProvider userProvider, ActivationStrategyProvider activationStrategyProvider) {
        this.name = name;
        this.featureProvider = featureProvider;
        this.stateRepository = stateRepository;
        this.userProvider = userProvider;
        this.strategyProvider = activationStrategyProvider;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<Feature> getFeatures() {
        return Collections.unmodifiableSet(featureProvider.getFeatures());
    }

    @Override
    public FeatureMetaData getMetaData(Feature feature) {
        Validate.notNull(feature, "feature is required");
        FeatureMetaData metadata = featureProvider.getMetaData(feature);
        if (metadata != null) {
            return metadata;
        }
        return new EmptyFeatureMetaData(feature);
    }

    @Override
    public boolean isActive(Feature feature) {

        Validate.notNull(feature, "feature is required");

        FeatureState state = stateRepository.getFeatureState(feature);

        if (state == null) {
            state = getMetaData(feature).getDefaultFeatureState();
        }

        if (state.isEnabled()) {

            // if no strategy is selected, the decision is simple
            String strategyId = state.getStrategyId();
            if (strategyId == null || strategyId.isEmpty()) {
                return true;
            }

            FeatureUser user = userProvider.getCurrentUser();

            // check the selected strategy
            for (ActivationStrategy strategy : strategyProvider.getActivationStrategies()) {
                if (strategy.getId().equalsIgnoreCase(strategyId)) {
                    return strategy.isActive(state, user);
                }
            }
        }

        // if the strategy was not found, the feature should be off
        return false;

    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        Validate.notNull(feature, "feature is required");
        FeatureState state = stateRepository.getFeatureState(feature);
        if (state == null) {
            state = getMetaData(feature).getDefaultFeatureState();
        }
        return state;
    }

    @Override
    public void setFeatureState(FeatureState state) {
        Validate.notNull(state, "state is required");
        stateRepository.setFeatureState(state);
    }

    @Override
    public List<ActivationStrategy> getActivationStrategies() {
        return strategyProvider.getActivationStrategies();
    }


    @Override
    public FeatureUser getCurrentFeatureUser() {
        return userProvider.getCurrentUser();
    }

    @Override
    public String toString() {
        return "DefaultFeatureManager[" + getName() + "]";
    }

}
