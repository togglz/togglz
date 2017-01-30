package org.togglz.core.activation;

import java.util.ArrayList;
import java.util.List;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;

/**
 * <p>
 * An abstract activation strategy that is designed to support cases where a specific parameter contains comma-separated
 * tokens that can be negated by prefixing the value with the NOT operator ({@code !}).
 * </p>
 * <p>
 * {@code AbstractTokenizedActivationStrategy} makes no real assumptions on how implementations will use the tokens to
 * determine whether the feature is active and, instead, simply tokenizes the parameter value. It even allows for the
 * token values to be transformed during this process by specifying a {@link TokenTransformer}.
 * </p>
 * <p>
 * Implementations are responsible for honoring the negated state of any tokens. Fortunately, this is very simple to do:
 * </p>
 * <pre>
 * &#064;Override
 * protected boolean isActive(FeatureState featureState, FeatureUser user, List&lt;Token&gt; tokens) {
 *     for (Token token : tokens) {
 *         boolean active = doSomeCheckOnTokenValue(token.getValue());
 *         if (active != token.isNegated()) {
 *             return true;
 *         }
 *     }
 *
 *     return false;
 * }
 * </pre>
 *
 * @author Alasdair Mercer
 * @see #getTokenParameterName()
 * @see #getTokenParameterTransformer()
 */
public abstract class AbstractTokenizedActivationStrategy implements ActivationStrategy {

    @Override
    public final boolean isActive(FeatureState featureState, FeatureUser user) {
        List<Token> tokens = tokenize(featureState, getTokenParameterName(), getTokenParameterTransformer());
        return isActive(featureState, user, tokens);
    }

    /**
     * <p>
     * This method is called by {@link #isActive(FeatureState, FeatureUser)} with the parsed {@code tokens} to make the
     * decision as to whether the feature is active.
     * </p>
     *
     * @param featureState
     *     the {@link FeatureState} which represents the current configuration of the feature
     * @param user
     *     the {@link FeatureUser user} for which to decide whether the feature is active (may be {@literal null})
     * @param tokens
     *     the {@code List} of {@link Token Tokens} parsed from the parameter value
     * @return {@literal true} if the feature should be active; otherwise {@literal false}.
     */
    protected abstract boolean isActive(FeatureState featureState, FeatureUser user, List<Token> tokens);

    /**
     * <p>
     * Looks up and tokenizes the value of the parameter with the given name on the feature.
     * </p>
     * <p>
     * If {@code transformer} is not {@literal null}, it will be asked to transform each individual token value.
     * </p>
     *
     * @param featureState
     *     the {@link FeatureState} which represents the current configuration of the feature
     * @param parameterName
     *     the name of the parameter whose value is to be tokenized
     * @param transformer
     *     the {@link TokenTransformer} to be used to transform the value of each token (may be {@literal null} to use
     *     the token values as-provided)
     * @return A {@code List} of {@link Token Tokens} extracted from the value of the parameter with the specified name.
     */
    protected List<Token> tokenize(FeatureState featureState, String parameterName, TokenTransformer transformer) {
        List<String> values = Strings.splitAndTrim(featureState.getParameter(parameterName), "[\\s,]+");
        List<Token> tokens = new ArrayList<>(values.size());
        for (String value : values) {
            if (transformer != null) {
                value = transformer.transform(value);
            }

            boolean negated = value.startsWith("!");
            if (negated) {
                value = value.substring(1);
            }

            tokens.add(new Token(value, negated));
        }

        return tokens;
    }

    /**
     * <p>
     * Returns the name of the parameter whose value is to be tokenized.
     * </p>
     *
     * @return The name of the parameter containing tokens.
     */
    public abstract String getTokenParameterName();

    /**
     * <p>
     * Returns the transformer to be used to transform the value of each {@link Token}.
     * </p>
     * <p>
     * By default, this method returns {@literal null}, meaning that the token values are used as-provided.
     * </p>
     *
     * @return The {@link TokenTransformer} to be used to transform token values or {@literal null} to use the original
     * values.
     */
    public TokenTransformer getTokenParameterTransformer() {
        return null;
    }

    /**
     * <p>
     * Contains information for a specific token including the token value and whether it has been negated.
     * </p>
     */
    public static final class Token {

        private final boolean negated;
        private final String value;

        private Token(String value, boolean negated) {
            this.value = value;
            this.negated = negated;
        }

        /**
         * <p>
         * Returns whether or not this {@link Token} is negated.
         * </p>
         *
         * @return {@literal true} if this token is negated; otherwise {@literal false}.
         */
        public boolean isNegated() {
            return negated;
        }

        /**
         * <p>
         * Returns the value for this {@link Token}.
         * </p>
         *
         * @return The value.
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * <p>
     * Used to transform a given {@link Token} value.
     * </p>
     * <p>
     * For example; if the tokens were to be used to perform a case-insensitive lookup, you might use a
     * {@code TokenTransformer} to transform the values to lower case up-front to reduce the cost of these lookups.
     * </p>
     * <pre>
     * &#064;Override
     * protected TokenTransformer getTokenParameterTransformer() {
     *     return new TokenTransformer() {
     *         &#064;Override
     *         public String transform(String value) {
     *             return value.toLowerCase();
     *         }
     *     };
     * }
     * </pre>
     */
    public interface TokenTransformer {

        /**
         * <p>
         * Transforms the token {@code value} provided.
         * </p>
         *
         * @param value
         *     the {@link Token} value to be transformed
         * @return The transformed {@code value}.
         */
        String transform(String value);
    }
}
