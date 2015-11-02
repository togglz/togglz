package org.togglz.archaius.repository;

import org.togglz.core.repository.property.PropertyBasedStateRepository;

/**
 * <p>
 * A {@link StateRepository} that gets its values from the Netflix Archaius framework.
 * </p>
 * <p>
 * See {@link PropertyBasedStateRepository} for information on the format of the property keys and
 * values.  The {@link DefaultReadOnlyArchaiusPropertySource} is used to manage them
 * with a default Archaius configuration; if you're using a non-default configuration you might
 * consider using {@code PropertyBasedStateRepository}, passing in your own implementation
 * of {@code DefaultReadOnlyArchaiusPropertySource} that overrides
 * {@link DefaultReadOnlyArchaiusPropertySource#getKeysStartingWith} to inspect the correct
 * backing store type.
 * </p>
 */
public class ArchaiusStateRepository extends PropertyBasedStateRepository {

    public ArchaiusStateRepository() {
        
        super(new DefaultReadOnlyArchaiusPropertySource());
    }
}
