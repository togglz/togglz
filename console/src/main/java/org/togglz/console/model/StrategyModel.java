package org.togglz.console.model;

import java.util.ArrayList;
import java.util.List;

import org.togglz.core.spi.ActivationStrategy;

public class StrategyModel {

    private final int index;

    private final ActivationStrategy strategy;

    private final FeatureModel featureModel;

    private final List<ParameterModel> parameters = new ArrayList<>();

    public StrategyModel(int index, ActivationStrategy strategy, FeatureModel featureModel) {
        this.index = index;
        this.strategy = strategy;
        this.featureModel = featureModel;
    }

    public String getLabel() {
        return strategy.getName();
    }

    public String getId() {
        return strategy.getId();
    }

    public ActivationStrategy getStrategy() {
        return strategy;
    }

    public boolean isSelected() {
        return featureModel.getStrategy() != null &&
            featureModel.getStrategy().getId().equals(getId());
    }

    public void add(ParameterModel param) {
        this.parameters.add(param);
    }

    public List<ParameterModel> getParameters() {
        return parameters;
    }

    public int getStrategyIndex() {
        return index;
    }

    public boolean isHasParametersWithValues() {
        for (ParameterModel param : parameters) {
            if (param.isHasValue()) {
                return true;
            }
        }
        return false;
    }

}