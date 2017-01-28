package org.togglz.rest.api.model;

import java.io.Serializable;

import org.togglz.core.repository.FeatureState;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonInclude(Include.NON_NULL)
public class FeatureToggleRepresentation implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty
    @JacksonXmlProperty
    private String name;
    
    @JsonProperty
    @JacksonXmlProperty
    private Boolean enabled;
    
    @JsonProperty(required=false)
    @JacksonXmlProperty
    private ActivationStrategyRepresentation strategy;
    
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
    
    public static FeatureToggleRepresentation of(FeatureState featureState) {
        FeatureToggleRepresentation featureToggle = new FeatureToggleRepresentation();
        featureToggle.setName(featureState.getFeature().name());
        featureToggle.setEnabled(featureState.isEnabled());
        if(featureState.getStrategyId() != null) {
            featureToggle.strategy = ActivationStrategyRepresentation.of(featureState);
        }
        return featureToggle;
    }

    @JsonIgnore
    public boolean hasActivationStrategy() {
        return this.strategy != null;
    }

    @JsonIgnore
    public String getStrategyId() {
        return this.strategy.id();
    }

    @JsonIgnore
    public Iterable<String> getParameterNames() {
        return this.strategy.getParameterNames();
    }

    @JsonIgnore
    public String getParameter(String paramName) {
        return this.strategy.getParameter(paramName);
    }

    
}
