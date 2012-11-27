package org.togglz.console.handlers.edit;

import java.util.ArrayList;
import java.util.List;

import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.util.Strings;

public class StrategyModel implements Comparable<StrategyModel> {

    private final int index;

    private final ActivationStrategy strategy;

    private final FeatureModel featureModel;

    private final List<ParameterModel> parameters = new ArrayList<ParameterModel>();

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

    public boolean hasId(String id) {
        return Strings.isNotBlank(id) && getId().equals(id);
    }

    public boolean isSelected() {
        return hasId(featureModel.getStrategyId());
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

    @Override
    public int compareTo(StrategyModel o) {
        return getLabel().compareTo(o.getLabel());
    }

}