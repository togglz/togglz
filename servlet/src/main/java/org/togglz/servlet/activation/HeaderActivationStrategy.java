package org.togglz.servlet.activation;

import org.togglz.core.activation.Parameter;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.servlet.util.HttpServletRequestHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

public class HeaderActivationStrategy implements ActivationStrategy {

    static final String ID = "header";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Header";
    }

    @Override
    public boolean isActive(FeatureState featureState, FeatureUser user) {
        HttpServletRequest request = HttpServletRequestHolder.get();
        if (request == null || featureState == null || featureState.getFeature() == null) {
            return false;
        }
        String header = request.getHeader("X-Features");
        if (header == null) {
            return false;
        }
        String[] split = header.split(",");
        return Arrays.stream(split).anyMatch(feature -> feature.equals(featureState.getFeature().name()));
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[0];
    }
}
