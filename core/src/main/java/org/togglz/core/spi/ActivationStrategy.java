package org.togglz.core.spi;

import org.togglz.core.activation.Parameter;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;

public interface ActivationStrategy {

    String getId();
    
    String getName();

    boolean isActive(FeatureState featureState, FeatureUser user);

    Parameter[] getParameters();

}
