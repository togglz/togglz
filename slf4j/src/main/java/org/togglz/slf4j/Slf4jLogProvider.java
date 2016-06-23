package org.togglz.slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.logging.Log;
import org.togglz.core.spi.LogProvider;

/**
 * 
 * Provider for integrating with SLF4J.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class Slf4jLogProvider implements LogProvider {

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public Log getLog(String name) {
        return new SLF4JLog(name);
    }

    public static class SLF4JLog implements Log {

        private final Logger log;

        public SLF4JLog(String name) {
            this.log = LoggerFactory.getLogger(name);
        }

        @Override
        public boolean isDebugEnabled() {
            return log.isDebugEnabled();
        }

        @Override
        public void debug(String msg) {
            log.debug(msg);
        }

        @Override
        public void info(String msg) {
            log.info(msg);
        }

        @Override
        public void warn(String msg) {
            log.warn(msg);
        }

        @Override
        public void error(String msg) {
            log.error(msg);
        }

        @Override
        public void error(String msg, Throwable e) {
            log.error(msg, e);
        }

    }

}
