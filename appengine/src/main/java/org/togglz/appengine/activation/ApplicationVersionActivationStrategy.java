package org.togglz.appengine.activation;

import com.google.appengine.api.utils.SystemProperty;
import org.togglz.core.activation.Parameter;
import org.togglz.core.activation.ParameterBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;

import java.util.List;

/**
 * Activation strategy that allows to activate features only for certain appengine application versions.
 * 
 * @author Fábio Franco Uechi
 */
public class ApplicationVersionActivationStrategy implements ActivationStrategy {

    public static final String ID = "gae_app_version";
    public static final String PARAM_VERSIONS = "version";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Appengine Application Versions";
    }

    @Override
    public boolean isActive(FeatureState featureState, FeatureUser user) {
        String allowedVersionsParam = featureState.getParameter(PARAM_VERSIONS);
        if (Strings.isNotBlank(allowedVersionsParam)) {
            String currentVersion = SystemProperty.applicationVersion.get();
            if (Strings.isNotBlank(currentVersion)) {
                List<String> allowedVersions = Strings.splitAndTrim(allowedVersionsParam, "[\\s,]+");
                for (String allowedVersion : allowedVersions) {
                    if (currentVersion.startsWith(allowedVersion)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
                ParameterBuilder.create(PARAM_VERSIONS).label("Application Versions")
                    .description("A comma-separated list of application versions for which the feature should be active.")
        };
    }

}