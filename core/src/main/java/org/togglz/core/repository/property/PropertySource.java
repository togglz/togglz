package org.togglz.core.repository.property;

import java.util.Set;

/**
 * Manages a source of property-like values.
 */
public interface PropertySource {

    /**
     * Reloads the properties from the source if they have been changed since the last load.
     */
    void reloadIfUpdated();
    
    /**
     * Returns the keys from the source that start with the specified prefix.
     * 
     * @param prefix the prefix to find
     * @return the keys starting with the specified prefix; an empty collection if no keys are found
     */
    Set<String> getKeysStartingWith(String prefix);
    
    /**
     * Returns the value of the specified key.  If the key is not found the specified default value
     * is returned.
     * 
     * @param key the key
     * @param defaultValue the default value if the key is not found
     * @return the value for the key
     */
    String getValue(String key, String defaultValue);

    /**
     * Returns a class suitable for editing the properties in the underlying representation in a
     * thread-safe manner.
     * 
     * @return an editor instance
     * @throws UnsupportedOperationException if this property source does not support updates
     */
    Editor getEditor();

    /**
     * Provides a means to update the underlying store in a thread-safe manner.
     */
    interface Editor {
        
        /**
         * Sets the specified key to the specified value.
         * 
         * @param key the key
         * @param value the value
         */
        void setValue(String key, String value);
        
        /**
         * Removes all keys with the specified prefix.
         * 
         * @param prefix the key prefix to remove
         */
        void removeKeysStartingWith(String prefix);
        
        /**
         * Saves all changes to the underlying store.
         */
        public void commit();
    }

}
