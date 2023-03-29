package org.togglz.spring.boot.actuate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.lang.Nullable;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzFeature;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzFeatureMetaData;

import java.util.Map;

/**
 * Extension to the Togglz endpoint for HTTP so the status coddes for write operations are correct.
 */
@EndpointWebExtension(endpoint = TogglzEndpoint.class)
public class TogglzEndpointWebExtension extends AbstractTogglzEndpoint {

    private final Logger log = LoggerFactory.getLogger(TogglzEndpointWebExtension.class);

    public TogglzEndpointWebExtension(FeatureManager featureManager) {
        super(featureManager);
    }

    @WriteOperation
    public WebEndpointResponse<TogglzFeature> setFeatureState(@Selector String name, @Nullable Boolean enabled,
                                                              @Nullable String strategy, @Nullable String parameters) {
        Feature feature = findFeature(name);
        if (feature == null) {
            return new WebEndpointResponse<>(WebEndpointResponse.STATUS_NOT_FOUND);
        }
        try {
            Map<String, String> parametersMap = parseParameterMap(parameters);
            FeatureState featureState = changeFeatureStatus(feature, enabled, strategy, parametersMap);
            FeatureMetaData metaData = this.featureManager.getMetaData(feature);
            return new WebEndpointResponse<>(new TogglzFeature(feature, featureState, new TogglzFeatureMetaData(metaData)));
        } catch (IllegalArgumentException exception) {
            log.debug("Illegal params format in togglz endpoint.", exception);
            return new WebEndpointResponse<>(WebEndpointResponse.STATUS_BAD_REQUEST);
        }
    }
}
