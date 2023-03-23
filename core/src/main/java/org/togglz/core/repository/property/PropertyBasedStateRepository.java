package org.togglz.core.repository.property;

import java.util.List;
import java.util.Properties;

import org.togglz.core.Feature;
import org.togglz.core.activation.UsernameActivationStrategy;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.util.Strings;

/**
 * This implementation of {@link StateRepository} stores the state of features in a {@link Properties} format.
 * The properties are managed through an implementation of {@link PropertySource} to manage the actual source
 * of the property values.
 * <p>
 * The file format has changed since version 2.0.0 because of the new extendable activation strategy support. Old file formats
 * will be automatically migrated. The new format looks like this:
 * </p>
 *
 * <pre>
 * FEATURE_ONE = true
 * FEATURE_ONE.strategy = gradual
 * FEATURE_ONE.param.percentage = 25
 * FEATURE_TWO = false
 * </pre>
 *
 * <p>
 * A feature is enabled if the value is one of {@code true}, {@code yes}, {@code enable}, or {@code enabled}; any other
 * value and the feature is considered disabled.
 * </p>
 */
public class PropertyBasedStateRepository implements StateRepository {

    private final PropertySource propertySource;

    public PropertyBasedStateRepository(PropertySource propertySource) {

        this.propertySource = propertySource;
    }

    public synchronized FeatureState getFeatureState(Feature feature) {

        // update file if changed
        propertySource.reloadIfUpdated();

        // if we got this one, the feature is present in the repository
        String enabledAsStr = propertySource.getValue(getEnabledPropertyName(feature), null);

        if (enabledAsStr != null) {

            // new state instance
            FeatureState state = new FeatureState(feature);
            state.setEnabled(isTrue(enabledAsStr));

            // active strategy (may be null)
            String strategy = propertySource.getValue(getStrategyPropertyName(feature), null);
            state.setStrategyId(strategy);

            // all parameters
            String paramPrefix = getParameterPropertyName(feature, "");
            for (String key : propertySource.getKeysStartingWith(paramPrefix)) {
                String id = key.substring(paramPrefix.length());
                String value = propertySource.getValue(key, null);
                state.setParameter(id, value);
            }

            /*
             * Backwards compatibility: if there are users stored in the old format, add them to the corresponding property
             */
            List<String> additionalUsers = toList(propertySource.getValue(getUsersPropertyName(feature), null));
            if (!additionalUsers.isEmpty()) {

                // join the users to one list and update the property
                List<String> currentUsers = toList(state.getParameter(UsernameActivationStrategy.PARAM_USERS));
                currentUsers.addAll(additionalUsers);
                state.setParameter(UsernameActivationStrategy.PARAM_USERS, Strings.join(currentUsers, ","));

                // we should set strategy id if it is not yet set
                if (state.getStrategyId() == null) {
                    state.setStrategyId(UsernameActivationStrategy.ID);
                }

            }

            return state;

        }

        // the feature is not configured in the repository
        return null;

    }

    public synchronized void setFeatureState(FeatureState featureState) {

        // update file if changed
        propertySource.reloadIfUpdated();

        Feature feature = featureState.getFeature();
        PropertySource.Editor editor = propertySource.getEditor();

        // enabled
        String enabledKey = getEnabledPropertyName(feature);
        String enabledValue = featureState.isEnabled() ? "true" : "false";
        editor.setValue(enabledKey, enabledValue);

        // write strategy id, will be removed if it is null
        editor.setValue(getStrategyPropertyName(feature), featureState.getStrategyId());

        // parameters
        String paramPrefix = getParameterPropertyName(feature, "");
        editor.removeKeysStartingWith(paramPrefix);
        for (String id : featureState.getParameterNames()) {
            String key = getParameterPropertyName(feature, id);
            editor.setValue(key, featureState.getParameter(id));
        }

        // remove the old users property if it still exists from the old format
        editor.setValue(getUsersPropertyName(feature), null);

        // write
        editor.commit();

    }

    private static String getEnabledPropertyName(Feature feature) {
        return feature.name();
    }

    private static String getStrategyPropertyName(Feature feature) {
        return feature.name() + ".strategy";
    }

    private static String getParameterPropertyName(Feature feature, String parameter) {
        return feature.name() + ".param." + parameter;
    }

    private static String getUsersPropertyName(Feature feature) {
        return feature.name() + ".users";
    }

    private static boolean isTrue(String s) {
        return s != null
            && ("true".equalsIgnoreCase(s.trim()) || "yes".equalsIgnoreCase(s.trim())
                || "enabled".equalsIgnoreCase(s.trim()) || "enable".equalsIgnoreCase(s.trim()));
    }

    private static List<String> toList(String input) {
        return Strings.splitAndTrim(input, ",");
    }

}
