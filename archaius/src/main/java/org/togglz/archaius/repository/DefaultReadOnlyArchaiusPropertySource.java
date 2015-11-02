package org.togglz.archaius.repository;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.togglz.core.repository.property.PropertyBasedStateRepository;
import org.togglz.core.repository.property.PropertySource;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.DynamicPropertyFactory;

/**
 * <p>
 * An implementation of a Togglz {@link PropertySouce} for use with a {@link PropertyBasedStateRepository}.
 * </p>
 * <p>
 * This implementation is for a read-only repository; attempts to update the state by calling {@link #getEditor()} throws
 * an {@code UnsupportedOperationException}.
 * </p>
 * 
 * @see ArchaiusStateRepository
 */
public class DefaultReadOnlyArchaiusPropertySource implements PropertySource {

    public DefaultReadOnlyArchaiusPropertySource() {
    }

    @Override
    public void reloadIfUpdated() {
        
        // nothing to do since Archaius is responsible for managing updates to state
    }

    @Override
    public Set<String> getKeysStartingWith(String prefix) {

        ConcurrentCompositeConfiguration sourceConfig
            = (ConcurrentCompositeConfiguration)DynamicPropertyFactory.getBackingConfigurationSource();

        // apache commons collections (and hence archaius) automatically appends the final
        // "." on the prefix; e.g., a prefix of "foo" matches "foo.1", "foo.2", etc.
        // so we need to strip off the final "."
        String trimmedPrefix = prefix.substring(0, prefix.length() - 1);

        Set<String> keys = new HashSet<String>();
        for (Iterator<String> keyIter = sourceConfig.getKeys(trimmedPrefix); keyIter.hasNext();) {
            String key = keyIter.next();
            keys.add(key);
        }

        return keys;
    }

    @Override
    public String getValue(String key, String defaultValue) {
        
        return DynamicPropertyFactory.getInstance().getStringProperty(key, defaultValue).get();
    }

    /**
     * Always throws an UnsupportedOperationException as changes to the state are handled by Archaius
     * and not by Togglz.
     */
    public Editor getEditor() {
        
        throw new UnsupportedOperationException("Updates through Togglz are not supported");
    }
}
