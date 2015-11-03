package org.togglz.rest.api.model;

import java.util.Map;
import java.util.Set;

import org.togglz.core.repository.FeatureState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@JsonInclude(Include.NON_NULL)
public class ActivationStrategyRepresentation {

    @JsonProperty
    @JacksonXmlProperty
    private String id;

    @JsonIgnore
    private Map<String, Parameter> parametersMap = Maps.newHashMap();

    private ActivationStrategyRepresentation() {
    }

    @JsonCreator
    public ActivationStrategyRepresentation(@JsonProperty("id") String id, @JsonProperty("parameters") Set<Parameter> parameters) {
        super();
        this.id = id;
        if (parameters != null) {
            for (Parameter param : parameters) {
                parametersMap.put(param.name, param);
            }
        }
    }

    @JsonProperty("parameters")
    public Set<Parameter> getParameters() {
        return Sets.newHashSet(parametersMap.values());
    }

    public String id() {
        return id;
    }

    @JsonIgnore
    public Iterable<String> getParameterNames() {
        return parametersMap.keySet();
    }

    @JsonIgnore
    public String getParameter(String paramName) {
        return parametersMap.get(paramName).value;
    }

    @JsonInclude(Include.NON_NULL)
    public static class Parameter {
        @JsonProperty
        String name;
        @JsonProperty
        String value;

        @JsonCreator
        public Parameter(@JsonProperty("name") String name, @JsonProperty("value") String value) {
            this.name = name;
            this.value = value;
        }
    }

    public static ActivationStrategyRepresentation of(FeatureState featureState) {
        Optional<ActivationStrategyRepresentation> activationStrategyRepresentation = Optional.absent();
        if (!Strings.isNullOrEmpty(featureState.getStrategyId())) {
            activationStrategyRepresentation = Optional.of(new ActivationStrategyRepresentation());
            activationStrategyRepresentation.get().id = featureState.getStrategyId();
            for (String paramName : featureState.getParameterNames()) {
                activationStrategyRepresentation.get().parametersMap.put(paramName,
                    new Parameter(paramName, featureState.getParameter(paramName)));
            }
        }
        return activationStrategyRepresentation.orNull();
    }

}
