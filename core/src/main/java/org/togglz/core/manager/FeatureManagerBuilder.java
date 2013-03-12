package org.togglz.core.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.togglz.core.Feature;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.spi.BeanFinder;
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
     * 
     * The method will use the {@link BeanFinder} SPI to find a {@link TogglzConfig} instance to use for configuration. The
     * method will throw a runtime exception if no or more than one {@link TogglzConfig} implementation is found.
     * 
     * @param context An optional context object which is provided to the {@link BeanFinder} implementations. This parameter is
     *        typically only used in web application to supply the ServletContext.
     */
    public FeatureManagerBuilder autoDiscovery(Object context) {

        ServiceLoader<BeanFinder> serviceLoader = ServiceLoader.load(BeanFinder.class);
        Iterator<BeanFinder> iterator = serviceLoader.iterator();

        List<TogglzConfig> configurations = new ArrayList<TogglzConfig>();

        while (iterator.hasNext()) {
            BeanFinder finder = (BeanFinder) iterator.next();

            Collection<TogglzConfig> result = finder.find(TogglzConfig.class, context);

            if (result != null) {
                configurations.addAll(result);
            }

        }

        if (configurations.size() != 1) {
            throw new IllegalStateException("Unable to find exactly one TogglzConfig but got "
                + configurations.size());
        }

        return togglzConfig(configurations.iterator().next());

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
