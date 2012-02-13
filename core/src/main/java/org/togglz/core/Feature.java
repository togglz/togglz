package org.togglz.core;

/**
 * 
 * This interface has to be implemented by feature enums
 * 
 * @author Christian Kaltepoth
 * 
 */
public interface Feature {

    /**
     * Returns a textual representation of the feature. This method is implicitly implemented as feature typically are enums.
     * 
     * @return Name of the feature
     */
    String name();

}
