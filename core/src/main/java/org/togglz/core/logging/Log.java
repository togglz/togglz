package org.togglz.core.logging;

/**
 * 
 * Togglz logging abstraction
 * 
 * @author Christian Kaltepoth
 * 
 */
public interface Log {

    boolean isDebugEnabled();

    void debug(String msg);

    void info(String msg);

    void warn(String msg);

    void error(String msg);

    void error(String msg, Throwable e);

}
