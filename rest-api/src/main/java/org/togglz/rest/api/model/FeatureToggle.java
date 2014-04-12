package org.togglz.rest.api.model;

import java.io.Serializable;

import org.togglz.core.repository.FeatureState;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonInclude(Include.NON_NULL)
public class FeatureToggle implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty
    @JacksonXmlProperty
    private String name;
    
    @JsonProperty
    @JacksonXmlProperty
    private Boolean enabled;
    
    @JsonProperty(required=false)
    @JacksonXmlProperty
    private ActivationStrategy strategy;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public static FeatureToggle from(FeatureState featureState) {
        FeatureToggle featureToggle = new FeatureToggle();
        featureToggle.setName(featureState.getFeature().name());
        featureToggle.setEnabled(featureState.isEnabled());
        if(featureState.getStrategyId() != null) {
            //featureState.get
        }
        return featureToggle;
    }
    
    
}
