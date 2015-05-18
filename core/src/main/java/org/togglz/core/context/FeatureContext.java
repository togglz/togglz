package org.togglz.core.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.WeakHashMap;

import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.spi.FeatureManagerProvider;
import org.togglz.core.util.Weighted;

/**
 *
 * This class is typically used to obtain the {@link FeatureManager} from application code. It uses the
 * {@link FeatureManagerProvider} to find the correct FeatureManager and caches it for each context class loader.
 *
 * @author Christian Kaltepoth
 *
 */
public class FeatureContext {

    private static final Log log = LogFactory.getLog(FeatureContext.class);

    /**
     * Cache for the {@link FeatureManager} instances looked up using the SPI
     */
    private static final Map<ClassLoader, FeatureManager> cache =
        Collections.synchronizedMap(new WeakHashMap<ClassLoader, FeatureManager>());

    /**
     *
     * Returns the {@link FeatureManager} for the current application (context class loader). The method uses the
     * {@link FeatureManagerProvider} SPI to find the correct {@link FeatureManager} instance. It will throw a runtime exception
     * if no {@link FeatureManager} can be found.
     *
     * @return The {@link FeatureManager} for the application, never <code>null</code>
     */
    public static FeatureManager getFeatureManager() {

        FeatureManager manager = getFeatureManagerOrNull();

        if (manager != null) {
            return manager;
        }

        throw new IllegalStateException("Could not find the FeatureManager. " +
            "For web applications please verify that the TogglzFilter starts up correctly. " +
            "In other deployment scenarios you will typically have to implement a FeatureManagerProvider " +
            "as described in the 'Advanced Configuration' chapter of the documentation.");

    }

    /**
     *
     * Returns the {@link FeatureManager} for the current application (context class loader). The method uses the
     * {@link FeatureManagerProvider} SPI to find the correct {@link FeatureManager} instance. If not manager could be found,
     * <code>null</code> is returned.
     *
     * @return The {@link FeatureManager} for the application or <code>null</code>
     */
    public static FeatureManager getFeatureManagerOrNull() {

        // the classloader used for cache lookups
        ClassLoader classLoader = getContextClassLoader();

        // try to lookup from the cache
        FeatureManager featureManager = cache.get(classLoader);
        if (featureManager == null) {

            // multiple threads could reach this point, but only one should do the lookup
            synchronized (cache) {

                // do another check (double-checked locking)
                featureManager = cache.get(classLoader);
                if (featureManager == null) {

                    if (log.isDebugEnabled()) {
                        log.debug("No cached FeatureManager for class loader: " + classLoader);
                    }

                    // lookup the feature manager
                    featureManager = lookupFeatureManager();

                    if (featureManager != null) {
                        cache.put(classLoader, featureManager);
                    }

                }
            }
        }

        return featureManager;

    }

    private static FeatureManager lookupFeatureManager() {

        // build a sorted list of all SPI implementations
        Iterator<FeatureManagerProvider> providerIterator = ServiceLoader.load(FeatureManagerProvider.class).iterator();
        List<FeatureManagerProvider> providerList = new ArrayList<FeatureManagerProvider>();
        while (providerIterator.hasNext()) {
            providerList.add(providerIterator.next());
        }
        Collections.sort(providerList, new Weighted.WeightedComparator());

        if (log.isDebugEnabled()) {
            log.debug("Found " + providerList.size() + " FeatureManagerProvider implementations...");
        }

        FeatureManager featureManager = null;

        // try providers one by one to find a FeatureManager
        for (FeatureManagerProvider provider : providerList) {

            // call the SPI
            featureManager = provider.getFeatureManager();

            if (log.isDebugEnabled()) {
                if (featureManager != null) {
                    log.debug("Provider " + provider.getClass().getName() + " returned FeatureManager: "
                        + featureManager.getName());
                } else {
                    log.debug("No FeatureManager provided by " + provider.getClass().getName());
                }
            }

            // accept the first FeatureManager found
            if (featureManager != null) {
                break;
            }

        }

        return featureManager;

    }

    /**
     * Returns the context classloader of the current thread. Throws a runtime exception if no context classloader is available.
     */
    private static ClassLoader getContextClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            throw new IllegalStateException("Unable to get the context class loader for the current thread!");
        }
        return classLoader;
    }

    public static void clearCache() {
        cache.clear();
    }

}
