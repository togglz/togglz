package org.togglz.core.activation;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;

/**
 * <p>
 * An abstract activation strategy that is designed to support cases where the activation of a feature is driven based
 * on the value of environmental/contextual properties.
 * </p>
 * <p>
 * {@code AbstractPropertyDrivenActivationStrategy} allows the name of the property can be passed via the
 * "{@value #PARAM_NAME}" parameter and gracefully falls back on a property name that is derived from the
 * {@link Feature} itself (e.g. "{@value #DEFAULT_PROPERTY_PREFIX}FEATURE_NAME"). It will take care of the majority of
 * the work and only really requires implementations to provide the means to lookup the value of the property:
 * </p>
 * <pre>
 * &#064;Override
 * protected abstract String getPropertyValue(FeatureState featureState, FeatureUser user, String name) {
 *     return doSomeStuffToGetPropertyValue(name);
 * }
 * </pre>
 * <p>
 * By default, the value of the property will be converted into a boolean but implementations are free to override this,
 * where needed, by overriding the {@link #isActive(FeatureState, FeatureUser, String, String)} method. However, it
 * would be ideal if all implementations tried to support the same value formats to allow for a unified experience. The
 * default conversion will fail fast if the property value does not match any of the predefined boolean representations
 * by throwing an {@code IllegalArgumentException}.
 * </p>
 * <p>
 * All implementations should honor the rule that the feature should not be activated if no matching property is found.
 * </p>
 *
 * @author Alasdair Mercer
 * @see #getPropertyValue(FeatureState, FeatureUser, String)
 */
public abstract class AbstractPropertyDrivenActivationStrategy implements ActivationStrategy {

    private static final String DEFAULT_PROPERTY_PREFIX = "togglz.";
    private static final String DEFAULT_STATE_VALUE = "true";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_PROPERTY_VALUE = "value";

    /**
     * <p>
     * Returns the name of the property on which to base the activation of the feature.
     * </p>
     * <p>
     * This method will first attempt to use the value of the parameter with the name provided. If that does not return
     * a valid property name (i.e. non-blank), then a property name will be constructed using a common prefix
     * ("{@value #DEFAULT_PROPERTY_PREFIX}") and the name of the feature.
     * </p>
     *
     * @param featureState
     *     the {@link FeatureState} which represents the current configuration of the feature
     * @param parameterName
     *     the name of the parameter that potentially contains the property name
     * @return The name of the property.
     */
    protected String getPropertyName(FeatureState featureState, String parameterName) {
        String propertyName = featureState.getParameter(parameterName);
        if (Strings.isNotBlank(propertyName)) {
            return propertyName;
        }

        return DEFAULT_PROPERTY_PREFIX + featureState.getFeature().name();
    }

    /**
     * <p>
     * Returns the value of the property with the specified {@code name} on which to base the activation of the feature.
     * </p>
     *
     * @param featureState
     *     the {@link FeatureState} which represents the current configuration of the feature
     * @param user
     *     the {@link FeatureUser user} for which to decide whether the feature is active (may be {@literal null})
     * @param name
     *     the name of the property whose value is to be returned
     * @return The (raw) value of the property with the given {@code name} or {@literal null} if none could be found.
     */
    protected abstract String getPropertyValue(FeatureState featureState, FeatureUser user, String name);

    @Override
    public final boolean isActive(FeatureState featureState, FeatureUser user) {
        String propertyName = getPropertyName(featureState, getPropertyNameParam());
        String propertyValue = getPropertyValue(featureState, user, propertyName);

        return isActive(featureState, user, propertyName, propertyValue);
    }

    protected String getPropertyNameParam() {
        return PARAM_NAME;
    }

    /**
     * <p>
     * This method is called by {@link #isActive(FeatureState, FeatureUser)} with the property name and value to make
     * the decision as to whether the feature is active.
     * </p>
     * <p>
     * By default, this method will convert {@code propertyValue} into a boolean using
     * {@link Strings#toBoolean(String)} but implementations are free to override this, where needed. However, it would
     * be ideal if all implementations tried to support the same value formats to allow for a unified experience. The
     * default implementation throw an {@code IllegalArgumentException} if {@code propertyValue} does not match any of
     * the predefined boolean representations.
     * </p>
     * <p>
     * This method should never return {@literal true} if {@code propertyValue} is {@literal null}.
     * </p>
     *
     * @param featureState
     *     the {@link FeatureState} which represents the current configuration of the feature
     * @param user
     *     the {@link FeatureUser user} for which to decide whether the feature is active (may be {@literal null})
     * @param propertyName
     *     the name of the property on which to base the activation of the feature
     * @param propertyValue
     *     the (raw) value of the property on which to base the activation of the feature (may be {@literal null} if
     *     none was found)
     * @return {@literal true} if the feature should be active; otherwise {@literal false}.
     * @throws IllegalArgumentException
     *     If {@code propertyValue} is non-{@literal null} <b>and</b> does not match any of the predefined boolean
     *     representations.
     */
    protected boolean isActive(FeatureState featureState, FeatureUser user, String propertyName, String propertyValue) {
        String expectedValue = featureState.getParameter(PARAM_PROPERTY_VALUE);
        if(expectedValue == null) {
            expectedValue = DEFAULT_STATE_VALUE;
        }
        return Strings.isNotBlank(propertyValue) && expectedValue.equalsIgnoreCase(propertyValue);
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
            ParameterBuilder.create(PARAM_NAME)
                .optional()
                .label("Property Name")
                .description("The name of the property to be used to determine whether the feature is enabled."),
            ParameterBuilder.create(PARAM_PROPERTY_VALUE)
                .optional()
                .label("Property Value")
                .description("Enable the feature when this value matches the property value")
        };
    }
}
