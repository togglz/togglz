package org.togglz.core.manager;

import java.util.UUID;

import org.togglz.core.Feature;
import org.togglz.core.activation.ActivationStrategyProvider;
import org.togglz.core.activation.DefaultActivationStrategyProvider;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.UserProvider;
import org.togglz.core.util.Validate;

/**
 * Builder of {@link DefaultFeatureManager}.
 * 
 * @author Christian Kaltepoth
 */
public class FeatureManagerBuilder {

    private String name = UUID.randomUUID().toString();
    private FeatureProvider featureProvider = null;
    private StateRepository stateRepository = new InMemoryStateRepository();
    private UserProvider userProvider = new NoOpUserProvider();
    private ActivationStrategyProvider strategyProvider = new DefaultActivationStrategyProvider();

    /**
     * Create a new builder
     */
    public static FeatureManagerBuilder begin() {
        return new FeatureManagerBuilder();
    }

    /**
     * Use the supplied state repository for the feature manager.
     */
    public FeatureManagerBuilder stateRepository(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
        return this;
    }

    /**
     * Use {@link #featureEnum(Class)} instead.
     */
    @Deprecated
    public FeatureManagerBuilder featureClass(Class<? extends Feature> featureClass) {
        return this.featureEnum(featureClass);
    }

    /**
     * Use the supplied feature enum class for the feature manager. Same as calling {@link #featureProvider(FeatureProvider)}
     * with {@link EnumBasedFeatureProvider}. Please note calling this method also set the name of the feature manager to the
     * simple name of the feature enum's type.
     */
    public FeatureManagerBuilder featureEnum(Class<? extends Feature> featureEnum) {
        return featureEnums(featureEnum);
    }

    /**
     * Use the supplied feature enum classes for the feature manager. Same as calling {@link #featureProvider(FeatureProvider)}
     * with {@link EnumBasedFeatureProvider}. Please note calling this method also set the name of the feature manager to the
     * simple name of the first feature enum's type.
     */
    public FeatureManagerBuilder featureEnums(Class<? extends Feature>... featureEnum) {
        this.featureProvider = new EnumBasedFeatureProvider(featureEnum);
        this.name = "FeatureManager[" + featureEnum[0].getSimpleName() + "]";
        return this;
    }

    /**
     * Sets the name of the feature manager. This name will then be available thought {@link FeatureManager#getName()}.
     */
    public FeatureManagerBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets a {@link FeatureProvider} for the feature manager. Only useful if you don't use enums to declare your features.
     */
    public FeatureManagerBuilder featureProvider(FeatureProvider featureProvider) {
        this.featureProvider = featureProvider;
        return this;
    }

    /**
     * Use the supplied {@link UserProvider} for the feature manager.
     */
    public FeatureManagerBuilder userProvider(UserProvider userProvider) {
        this.userProvider = userProvider;
        return this;
    }

    /**
     * Use the supplied {@link ActivationStrategyProvider} for the activation strategies;
     */
    public FeatureManagerBuilder activationStrategyProvider(ActivationStrategyProvider strategyProvider) {
        this.strategyProvider = strategyProvider;
        return this;
    }

    /**
     * Adds an additional {@link ActivationStrategy} to the current {@link ActivationStrategyProvider}. This currently only
     * works if you are using the {@link DefaultActivationStrategyProvider}.
     */
    public FeatureManagerBuilder activationStrategy(ActivationStrategy strategy) {
        if (strategyProvider instanceof DefaultActivationStrategyProvider) {
            ((DefaultActivationStrategyProvider) strategyProvider).addActivationStrategy(strategy);
            return this;
        }
        throw new IllegalStateException("Adding ActivationStrategies is only allowed when using "
            + DefaultActivationStrategyProvider.class.getSimpleName());
    }

    /**
     * Initialize the builder with the configuration from the supplied {@link TogglzConfig} instance.
     */
    public FeatureManagerBuilder togglzConfig(TogglzConfig config) {
        stateRepository(config.getStateRepository());
        featureEnum(config.getFeatureClass());
        userProvider(config.getUserProvider());
        return this;
    }

    /**
     * Create the {@link FeatureManager} using the current configuration of the builder
     */
    public FeatureManager build() {
        Validate.notBlank(name, "No name specified");
        Validate.notNull(featureProvider, "No feature provider specified");
        Validate.notNull(stateRepository, "No state repository specified");
        Validate.notNull(userProvider, "No user provider specified");
        Validate.notNull(strategyProvider, "No activation strategy provider specified");
        return new DefaultFeatureManager(name, featureProvider, stateRepository, userProvider, strategyProvider);
    }

}
