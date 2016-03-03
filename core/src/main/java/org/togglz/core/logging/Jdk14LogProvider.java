package org.togglz.core.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.togglz.core.spi.LogProvider;

/**
 * 
 * Implementation of {@link LogProvider} for the standard JDK logging facilities.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class Jdk14LogProvider implements LogProvider {

    @Override
    public int priority() {
        return 1000;
    }

    @Override
    public Log getLog(String name) {
        return new Jdk14Log(name);
    }

    public static class Jdk14Log implements Log {

        private final Logger logger;

        public Jdk14Log(String name) {
            logger = Logger.getLogger(name);
        }

        @Override
        public boolean isDebugEnabled() {
            return logger.isLoggable(Level.FINE);
        }

        @Override
        public void debug(String msg) {
            logger.fine(msg);
        }

        @Override
        public void info(String msg) {
            logger.info(msg);
        }

        @Override
        public void warn(String msg) {
            logger.warning(msg);
        }

        @Override
        public void error(String msg) {
            logger.severe(msg);
        }

        @Override
        public void error(String msg, Throwable e) {
            logger.log(Level.SEVERE, msg, e);
        }

    }

}
