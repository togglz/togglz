package org.togglz.console.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.togglz.core.Feature;
import org.togglz.core.activation.Parameter;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.util.Strings;
import org.togglz.core.util.Validate;

public class FeatureModel {

    private final Feature feature;
    private final FeatureMetaData metadata;

    private final Map<String, String> attributes;
    private final String infoLink;

    private final List<StrategyModel> strategies = new ArrayList<StrategyModel>();

    private boolean enabled;

    private StrategyModel strategy;

    public FeatureModel(Feature feature, FeatureMetaData metadata, List<ActivationStrategy> impls) {

        this.feature = feature;
        this.metadata = metadata;

        this.attributes = new LinkedHashMap<String, String>(metadata.getAttributes());
        this.infoLink = this.attributes.remove("InfoLink");

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

        String strategyId = Strings.trimToNull(featureState.getStrategyId());
        this.strategy = getStrategyById(strategyId);

        this.enabled = featureState.isEnabled();

        for (ParameterModel param : getParameters()) {
            param.readValueFrom(featureState);
        }

    }

    public void restoreFromRequest(HttpServletRequest request) {

        String enabledParam = request.getParameter("enabled");
        this.enabled = enabledParam != null && enabledParam.trim().length() > 0;

        String strategyId = request.getParameter("strategy");
        this.strategy = getStrategyById(strategyId);

        for (ParameterModel param : getParameters()) {
            param.readValueFrom(request);
        }

    }

    public List<String> getValidationErrors() {
        List<String> errors = new ArrayList<>();

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
        List<ParameterModel> params = new ArrayList<>();
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

        if (strategy != null) {
            state.setStrategyId(strategy.getId());
            for (ParameterModel param : strategy.getParameters()) {
                state.setParameter(param.getId(), Strings.trimToNull(param.getValue()));
            }
        }
        return state;
    }

    public boolean isHasInfoLink() {
        return Strings.isNotBlank(infoLink);
    }

    public String getInfoLink() {
        return infoLink;
    }

    public boolean isHasAttributes() {
        return attributes.size() > 0;
    }

    public Set<Entry<String, String>> getAttributes() {
        return attributes.entrySet();
    }

    public List<StrategyModel> getStrategies() {
        return strategies;
    }

    public String getLabel() {
        return metadata.getLabel();
    }

    public String getName() {
        return feature.name();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public StrategyModel getStrategy() {
        return strategy;
    }

}
