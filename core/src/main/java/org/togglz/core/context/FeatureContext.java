package org.togglz.core.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
    private static final WeakHashMap<ClassLoader, FeatureManager> cache = new WeakHashMap<ClassLoader, FeatureManager>();

    /**
     * 
     * Returns the {@link FeatureManager} for the current application (context class loader). The method uses the
     * {@link FeatureManagerProvider} SPI to find the correct {@link FeatureManager} instance. It will throw a runtime exception
     * if no {@link FeatureManager} can be found.
     * 
     * @return The {@link FeatureManager} for the application, never <code>null</code>
     */
    public static synchronized FeatureManager getFeatureManager() {

        // the classloader used for cache lookups
        ClassLoader classLoader = getContextClassLoader();

        // first try to lookup from the cache
        FeatureManager featureManager = cache.get(classLoader);
        if (featureManager != null) {
            return featureManager;
        }

        if (log.isDebugEnabled()) {
            log.debug("No cached FeatureManager for class loader: " + classLoader);
        }

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

        // try providers one by one to find a FeatureManager
        for (FeatureManagerProvider provider : providerList) {

            // call the SPI
            featureManager = provider.getFeatureManager();

            if (log.isDebugEnabled()) {
                if (featureManager != null) {
                    log.debug("Provider " + provider.getClass().getName() + " returned a FeatureManager");
                } else {
                    log.debug("No FeatureManager provided by " + provider.getClass().getName());
                }
            }

            // accept the first FeatureManager found
            if (featureManager != null) {
                break;
            }

        }

        // cache the result for later lookups
        if (featureManager != null) {
            cache.put(classLoader, featureManager);
            return featureManager;
        }

        throw new IllegalStateException("Could not find the FeatureManager. " +
            "For web applications please verify that the TogglzFilter starts up correctly. " +
            "In other deployment scenarios you will typically have to implement a FeatureManagerProvider " +
            "as described in the 'Advanced Configuration' chapter of the documentation.");
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
