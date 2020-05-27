package org.togglz.core.activation;

import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ContextAwareActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;

import java.util.List;

/**
 * Activation strategy that allows to activate features only for certain strings.
 *
 * @author Philip Sanetra
 */
public class StringWhitelistActivationStrategy implements ContextAwareActivationStrategy<String> {

    public static final String ID = "string_whitelist";

    public static final String PARAM_WHITELIST = "whitelist";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "String whitelist";
    }

    @Override
    public boolean isActive(FeatureState state, FeatureUser user, String context) {

        String whitelistString = state.getParameter(PARAM_WHITELIST);

        if (context == null || Strings.isBlank(whitelistString)) {
            return false;
        }

        List<String> whitelist = Strings.splitAndTrim(whitelistString, ",");

        context = context.trim();

        if (context.isEmpty()) {
            return false;
        }

        return whitelist.stream().anyMatch(context::equals);
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
            ParameterBuilder
                .create(PARAM_WHITELIST)
                .label("Whitelist")
                .description("A comma-separated list of strings for which the feature is active.")
        };
    }

}
