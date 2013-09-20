package org.togglz.core.context;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.spi.FeatureManagerProvider;

/**
 * 
 * Implementation of {@link FeatureManagerProvider} that tries to look up a {@link FeatureManager} from JNDI using the name
 * <code>java:/comp/env/FeatureManager</code>.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class JNDIFeatureManagerProvider implements FeatureManagerProvider {

    public final static String JNDI_NAME = "java:/comp/env/FeatureManager";

    private final Log log = LogFactory.getLog(JNDIFeatureManagerProvider.class);

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
                log.debug("FeatureMananger not found: " + e.getMessage());
            }

        }

        return null;

    }

    private boolean isDisabled() {
        return "true".equals(System.getProperty("org.togglz.DISABLE_JNDI_LOOKUPS", ""));
    }

}
