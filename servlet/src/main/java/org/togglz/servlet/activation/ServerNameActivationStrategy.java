package org.togglz.servlet.activation;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.togglz.core.activation.Parameter;
import org.togglz.core.activation.ParameterBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;
import org.togglz.servlet.util.HttpServletRequestHolder;

/**
 * Activation strategy that will use the server name used in the request to decide if the feature is active or not.
 * 
 * This strategy can be useful when given service instance is available through two different virtual host names (like
 * www.example.com and beta.example.com) each with different features enabled.
 * 
 * @author Marcin Zajączkowski, 2014-04-28
 */
public class ServerNameActivationStrategy implements ActivationStrategy {

    // Visible for testing
    static final String ID = "servername";
    static final String PARAM_SERVER_NAMES = "servernames";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Server names (vhosts)";
    }

    @Override
    public boolean isActive(FeatureState featureState, FeatureUser user) {
        HttpServletRequest request = getServletRequest();
        if (request != null) {
            String allowedServerNamesParam = featureState.getParameter(PARAM_SERVER_NAMES);
            List<String> allowedServerNames = Strings.splitAndTrim(allowedServerNamesParam, "[\\s,]+");

            // TODO: This could support a wildcard domain name matching like *.beta.example.com
            return allowedServerNames.contains(request.getServerName());
        }
        return false;
    }

    // Visible for testing
    HttpServletRequest getServletRequest() {
        return HttpServletRequestHolder.get();
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
                ParameterBuilder.create(PARAM_SERVER_NAMES).label("Server names")
                    .description("A comma-separated list of server names (virtual hosts) used in request " +
                        "for which the feature should be active.")
        };
    }
}
