package org.togglz.core.bootstrap;

import java.util.Collection;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.spi.BeanFinder;
import org.togglz.core.util.Strings;
import org.togglz.core.util.Validate;

/**
 * 
 * <p>
 * This class is used to automatically bootstrap a {@link FeatureManager} for Togglz which is the default behavior for simple
 * web applications. The bootstrap process heavily relies on the {@link BeanFinder} SPI.
 * </p>
 * 
 * <p>
 * The bootstrapping process consists of the following steps:
 * </p>
 * 
 * <ol>
 * <li>First try to locate an implementation of the {@link TogglzBootstrap} interface. If this lookup is successful, a
 * {@link FeatureManager} is created from it and returned.</li>
 * <li>If the first step fails, the class will try to locate an implementation of the {@link TogglzConfig} interface. If an
 * implementation is found, it is used to build a {@link FeatureManager} using
 * {@link FeatureManagerBuilder#togglzConfig(TogglzConfig)} which will be returned to the caller.</li>
 * </ol>
 * 
 * @author Christian Kaltepoth
 * 
 */
public class FeatureManagerBootstrapper {

    /**
     * Tries to automatically build and configure a {@link FeatureManager} like described above.
     * 
     * @param context An optional context object which is provided to the {@link BeanFinder} implementations. This parameter is
     *        for example used in web application to supply the ServletContext.
     * @return A newly created {@link FeatureManager}
     * @throws IllegalStateException if the bootstrapping process failed
     */
    public FeatureManager createFeatureManager(Object context) {

        /*
         * Step 1: Try to find TogglzBootstrap implementation
         */
        TogglzBootstrap togglzBootstrap = lookupBean(TogglzBootstrap.class, context);

        if (togglzBootstrap != null) {
            FeatureManager manager = togglzBootstrap.createFeatureManager();
            Validate.notNull(manager, togglzBootstrap.getClass().getName() + " returned null");
            return manager;
        }

        /*
         * Steps 2: Fallback to use TogglzConfig implementation for FeatureManager configuration
         */
        TogglzConfig togglzConfig = lookupBean(TogglzConfig.class, context);

        if (togglzConfig != null) {
            return new FeatureManagerBuilder()
                .togglzConfig(togglzConfig)
                .build();
        }

        // fail with a descriptive error message
        throw new IllegalStateException("Could not find any implementation of TogglzConfig or TogglzBootstrap. " +
            "Please make sure that you have added the required integration modules to your project " +
            "or register the implementation in your web.xml as described in the 'Configuration' chapter of the documentation.");

    }

    private <T> T lookupBean(Class<T> clazz, Object context) {
        Set<T> impls = new HashSet<>();

        for (BeanFinder beanFinder : ServiceLoader.load(BeanFinder.class)) {
            Collection<T> result = beanFinder.find(clazz, context);
            if (result != null) {
                impls.addAll(result);
            }
        }

        if (impls.size() > 1) {
            throw new IllegalStateException("Found more than one implementation of the " +
                clazz.getSimpleName() + " interface: " + Strings.joinClassNames(impls));
        }

        if (!impls.isEmpty()) {
            return impls.iterator().next();
        }
        return null;

    }

}
