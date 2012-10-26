package org.togglz.core.spi;

import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Weighted;

public interface ActivationStrategy extends Weighted {

    boolean isActive(FeatureState featureState, FeatureUser user);

}
