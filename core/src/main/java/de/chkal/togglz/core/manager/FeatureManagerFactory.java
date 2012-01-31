package de.chkal.togglz.core.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import de.chkal.togglz.core.config.FeatureManagerConfiguration;
import de.chkal.togglz.core.spi.BeanFinder;

/**
 * 
 * This class is a factory for the {@link FeatureManager}. It will use the {@link BeanFinder} SPI to find an implementation of
 * the {@link FeatureManagerConfiguration} interface and will use this to configure the {@link FeatureManager}.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class FeatureManagerFactory {

    /**
     * 
     * Build a new {@link FeatureManager}. The method will use the {@link BeanFinder} SPI to find the required
     * {@link FeatureManagerConfiguration} instance to use for configuration. The method will throw a runtime exception if no or
     * more than one {@link FeatureManagerConfiguration} implementation is found.
     * 
     * @param context An optional context object which is provided to the {@link BeanFinder} implementations. This parameter is
     *        typically only used in web application to supply the ServletContext.
     * @return The new {@link FeatureManager}
     */
    public FeatureManager build(Object context) {

        ServiceLoader<BeanFinder> serviceLoader = ServiceLoader.load(BeanFinder.class);
        Iterator<BeanFinder> iterator = serviceLoader.iterator();

        List<FeatureManagerConfiguration> configurations = new ArrayList<FeatureManagerConfiguration>();

        while (iterator.hasNext()) {
            BeanFinder finder = (BeanFinder) iterator.next();

            Collection<FeatureManagerConfiguration> result = finder.find(FeatureManagerConfiguration.class, context);

            if (result != null) {
                configurations.addAll(result);
            }

        }

        if (configurations.size() != 1) {
            throw new IllegalStateException("Unable to find exactly on FeatureManagerConfiguration but got "
                    + configurations.size());
        }

        return new DefaultFeatureManager(configurations.iterator().next());
    }

}
