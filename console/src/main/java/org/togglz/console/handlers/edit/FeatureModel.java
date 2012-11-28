package org.togglz.console.handlers.edit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.togglz.core.Feature;
import org.togglz.core.FeatureMetaData;
import org.togglz.core.activation.Parameter;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.util.Strings;
import org.togglz.core.util.Validate;

public class FeatureModel {

    private final Feature feature;
    private final FeatureMetaData metaData;

    private final List<StrategyModel> strategies = new ArrayList<StrategyModel>();

    private boolean enabled;
    private String strategyId = null;

    public FeatureModel(Feature feature, List<ActivationStrategy> impls) {

        this.feature = feature;
        this.metaData = FeatureMetaData.build(feature);

        List<ActivationStrategy> sortedImpls = new ArrayList<ActivationStrategy>(impls);
        Collections.sort(sortedImpls, new Comparator<ActivationStrategy>() {
            @Override
            public int compare(ActivationStrategy o1, ActivationStrategy o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        int paramIndex = 1;
        int strategyIndex = 1;
        for (ActivationStrategy impl : sortedImpls) {

            StrategyModel strategy = new StrategyModel(strategyIndex++, impl, this);

            this.strategies.add(strategy);

            for (Parameter param : impl.getParameters()) {
                strategy.add(new ParameterModel(paramIndex++, param, strategy));
            }

        }

    }

    public void populateFromFeatureState(FeatureState featureState) {

        this.strategyId = Strings.trimToNull(featureState.getStrategyId());
        this.enabled = featureState.isEnabled();

        for (ParameterModel param : getParameters()) {
            param.readValueFrom(featureState);
        }

    }

    public void restoreFromRequest(HttpServletRequest request) {

        String enabledParam = request.getParameter("enabled");
        this.enabled = enabledParam != null && enabledParam.trim().length() > 0;
        this.strategyId = request.getParameter("strategy");

        for (ParameterModel param : getParameters()) {
            param.readValueFrom(request);
        }

    }

    public List<String> getValidationErrors() {

        List<String> errors = new ArrayList<String>();

        // check if the strategy ID is valid
        StrategyModel strategy = null;
        if (Strings.isNotBlank(strategyId)) {
            strategy = getStrategyById(strategyId);
            if (strategy == null) {
                errors.add("Invalid strategy selected: " + strategyId);
            }
        }

        // validate parameters of the strategy
        if (strategy != null) {
            for (ParameterModel param : strategy.getParameters()) {
                if (!param.isValid()) {
                    String msg = param.getValidationError();
                    if (msg != null) {
                        errors.add(msg);
                    }
                }
            }
        }

        return errors;

    }

    private StrategyModel getStrategyById(String id) {
        for (StrategyModel strategy : strategies) {
            if (strategy.getId().equals(id)) {
                return strategy;
            }
        }
        return null;
    }

    public List<ParameterModel> getParameters() {
        List<ParameterModel> params = new ArrayList<ParameterModel>();
        for (StrategyModel strategy : strategies) {
            params.addAll(strategy.getParameters());
        }
        return params;
    }

    public boolean isValid() {
        return getValidationErrors().isEmpty();
    }

    public FeatureState toFeatureState() {

        Validate.isTrue(getValidationErrors().isEmpty(),
            "Calling toFeatureState() is only allowed for a valid model");

        FeatureState state = new FeatureState(feature, enabled);

        if (Strings.isNotBlank(strategyId)) {

            StrategyModel strategy = getStrategyById(strategyId);
            Validate.notNull(strategy, "Strategy cannot be found: " + strategyId);

            state.setStrategyId(strategy.getId());

            for (ParameterModel param : strategy.getParameters()) {
                state.setParameter(param.getId(), Strings.trimToNull(param.getValue()));
            }

        }

        return state;
    }

    public List<StrategyModel> getStrategies() {
        return strategies;
    }

    public String getLabel() {
        return metaData.getLabel();
    }

    public String getName() {
        return feature.name();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getStrategyId() {
        return strategyId;
    }

    public String getLabelOfSelectedStrategy() {
        if(Strings.isNotBlank(strategyId)) {
            StrategyModel strategy = getStrategyById(strategyId);
            if(strategy != null) {
                return strategy.getLabel();
            }
        }
        return null;
    }

}
