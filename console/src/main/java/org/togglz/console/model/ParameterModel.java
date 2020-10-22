package org.togglz.console.model;

import javax.servlet.http.HttpServletRequest;

import org.owasp.encoder.Encode;
import org.togglz.core.activation.Parameter;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.Strings;

public class ParameterModel {

    private final int index;

    private final Parameter parameter;

    private final StrategyModel strategy;

    private String value;

    public ParameterModel(int index, Parameter parameter, StrategyModel strategy) {
        this.index = index;
        this.parameter = parameter;
        this.strategy = strategy;
    }

    public void readValueFrom(FeatureState featureState) {
        this.value = featureState.getParameter(parameter.getName());
    }

    public void readValueFrom(HttpServletRequest request) {
        this.value = request.getParameter(getInputId());
    }

    public String getValidationError() {
        if (Strings.isBlank(value) && !parameter.isOptional()) {
            return "Please enter a value for parameter: " + getLabel();
        }
        if (Strings.isNotBlank(value) && !parameter.isValid(value.trim())) {
            return "Please enter a valid value for: " + getLabel();
        }
        return null;
    }

    public boolean isValid() {
        return Strings.isBlank(getValidationError());
    }

    public String getLabel() {
        return parameter.getLabel();
    }

    public String getInputId() {
        return "p" + index;
    }

    public String getId() {
        return parameter.getName();
    }

    public String getValue() {
        return value == null ? "" : Encode.forHtml(value);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getStrategyIndex() {
        return strategy.getStrategyIndex();
    }

    public boolean isVisible() {
        return strategy.isSelected();
    }

    public String getDescription() {
        return parameter.getDescription();
    }

    public boolean isHasDescription() {
        return Strings.isNotBlank(getDescription());
    }

    public boolean isLargeText() {
        return parameter.isLargeText();
    }

    public boolean isHasValue() {
        return Strings.isNotBlank(value);
    }

}
