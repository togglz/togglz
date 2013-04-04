package org.togglz.core.manager;

import org.togglz.core.Feature;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.UserProvider;

/**
 * 
 * Builder of {@link DefaultFeatureManager}.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class FeatureManagerBuilder {

    private FeatureProvider featureProvider = null;
    private StateRepository stateRepository = new InMemoryStateRepository();
    private UserProvider userProvider = new NoOpUserProvider();

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
     * with {@link EnumBasedFeatureProvider}.
     */
    public FeatureManagerBuilder featureEnum(Class<? extends Feature> featureEnum) {
        this.featureProvider = new EnumBasedFeatureProvider(featureEnum);
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
        checkNotNull(featureProvider, "No feature provider specified");
        checkNotNull(stateRepository, "No state repository specified");
        checkNotNull(userProvider, "No user provider specified");
        return new DefaultFeatureManager(featureProvider, stateRepository, userProvider);
    }

    private static void checkNotNull(Object o, String message) {
        if (o == null) {
            throw new IllegalStateException(message);
        }
    }

}
