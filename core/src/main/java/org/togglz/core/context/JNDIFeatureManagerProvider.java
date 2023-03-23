package org.togglz.core.context;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.spi.FeatureManagerProvider;

/**
 *
 * Implementation of {@link FeatureManagerProvider} that tries to look up a {@link FeatureManager} from JNDI using the name
 * {@code java:/comp/env/FeatureManager}.
 *
 * @author Christian Kaltepoth
 *
 */
public class JNDIFeatureManagerProvider implements FeatureManagerProvider {

    public final static String JNDI_NAME = "java:/comp/env/FeatureManager";

    private final Logger log = LoggerFactory.getLogger(JNDIFeatureManagerProvider.class);

    @Override
    public int priority() {
        // runs AFTER the WebAppFeatureManagerProvider
        return 200;
    }

    @Override
    public FeatureManager getFeatureManager() {

        if (!isDisabled()) {

            try {

                InitialContext initialContext = new InitialContext();
                return (FeatureManager) initialContext.lookup(JNDI_NAME);

            } catch (NamingException e) {
                log.debug("FeatureManager not found: " + e.getMessage());
            }

        }

        return null;

    }

    private boolean isDisabled() {
        return "true".equals(System.getProperty("org.togglz.DISABLE_JNDI_LOOKUPS", ""));
    }

}
