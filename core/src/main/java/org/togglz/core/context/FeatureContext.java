package org.togglz.core.context;

import static org.togglz.core.util.ConcurrentReferenceHashMap.ReferenceType.STRONG;
import static org.togglz.core.util.ConcurrentReferenceHashMap.ReferenceType.WEAK;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.spi.FeatureManagerProvider;
import org.togglz.core.util.ConcurrentReferenceHashMap;
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

    private static final Logger log = LoggerFactory.getLogger(FeatureContext.class);

    /**
     * Cache for the {@link FeatureManager} instances looked up using the SPI
     */
    private static final ConcurrentReferenceHashMap<ClassLoader, FeatureManager> cache
            = new ConcurrentReferenceHashMap<>(WEAK, STRONG);

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
        ClassLoader classLoader = getContextClassLoader();
        FeatureManager featureManager = cache.get(classLoader);
        if (featureManager != null) {
            return featureManager;
        }
        featureManager = findFeatureManagerInClassLoader(classLoader);
        if (featureManager != null) {
            FeatureManager previousFeatureManager = cache.putIfAbsent(classLoader, featureManager);
            if (previousFeatureManager != null) {
                // Return FeatureManager that was inserted first
                return previousFeatureManager;
            }
        }
        return featureManager;
    }

    private static FeatureManager findFeatureManagerInClassLoader(ClassLoader classLoader) {
        if (log.isDebugEnabled()) {
            log.debug("No cached FeatureManager for class loader: " + classLoader);
        }

        // build a sorted list of all SPI implementations
        Iterator<FeatureManagerProvider> providerIterator = ServiceLoader.load(FeatureManagerProvider.class).iterator();
        List<FeatureManagerProvider> providerList = new ArrayList<>();
        while (providerIterator.hasNext()) {
            providerList.add(providerIterator.next());
        }
        providerList.sort(new Weighted.WeightedComparator());

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
