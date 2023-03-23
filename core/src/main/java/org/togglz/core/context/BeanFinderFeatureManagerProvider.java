package org.togglz.core.context;

import java.util.Collection;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.togglz.core.manager.FeatureManager;
import org.togglz.core.spi.BeanFinder;
import org.togglz.core.spi.FeatureManagerProvider;
import org.togglz.core.util.Strings;

/**
 * 
 * {@link FeatureManagerProvider} that uses the {@link BeanFinder} SPI to lookup the {@link FeatureManager}. This implementation
 * is especially useful if the FeatureManager should be created and managed by Spring or CDI instead of by the Togglz
 * bootstrapping process.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class BeanFinderFeatureManagerProvider implements FeatureManagerProvider {

    @Override
    public int priority() {
        return 60;
    }

    @Override
    public FeatureManager getFeatureManager() {

        // we _may_ find multiple managers, which will lead to an error
        Set<FeatureManager> managers = new HashSet<>();

        // ask all providers for managed FeatureManager
        for (BeanFinder beanFinder : ServiceLoader.load(BeanFinder.class)) {
            Collection<FeatureManager> result = beanFinder.find(FeatureManager.class, null);
            if (result != null) {
                managers.addAll(result);
            }
        }

        // more than once manager cannot be handled
        if (managers.size() > 1) {
            throw new IllegalStateException("Found more than one FeatureManager using the BeanFinder SPI: "
                + Strings.join(managers, ", "));
        }

        // return the manager or null
        if (!managers.isEmpty()) {
            return managers.iterator().next();
        }
        return null;

    }

}
