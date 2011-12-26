package de.chkal.togglz.core.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import de.chkal.togglz.core.config.FeatureManagerConfiguration;
import de.chkal.togglz.core.spi.BeanFinder;

public class FeatureManagerFactory {

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
