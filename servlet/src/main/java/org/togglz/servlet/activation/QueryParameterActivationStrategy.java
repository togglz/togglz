package org.togglz.servlet.activation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.activation.Parameter;
import org.togglz.core.activation.ParameterBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;
import org.togglz.servlet.util.HttpServletRequestHolder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class QueryParameterActivationStrategy implements ActivationStrategy {

    private static final Logger log = LoggerFactory.getLogger(QueryParameterActivationStrategy.class);

    public static final String PARAM_URL_PARAMS = "params";

    private static final Pattern SPLIT_ON_COMMA = Pattern.compile("\\s*,\\s*");
    private static final Pattern SPLIT_ON_EQUALS = Pattern.compile("\\s*=\\s*");

    @Override
    public String getId() {
        return "query-parameter";
    }

    @Override
    public String getName() {
        return "Query Parameter";
    }

    @Override
    public boolean isActive(FeatureState featureState, FeatureUser user) {

        HttpServletRequest request = HttpServletRequestHolder.get();
        if (request == null) {
            return false;
        }

        String triggerParamsString = featureState.getParameter(PARAM_URL_PARAMS);
        if (Strings.isBlank(triggerParamsString)) {
            return false;
        }
        String[] triggerParams = SPLIT_ON_COMMA.split(triggerParamsString.trim());

        Map<String, String[]> actualParams = new HashMap<>();
        actualParams.putAll(request.getParameterMap());
        String referer = request.getHeader("referer");
        if (referer != null) {
            for (Map.Entry<String, List<String>> entry : getRefererParameters(actualParams, referer).entrySet()) {
                actualParams.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
            }
        }

        return isTriggerParamPresent(actualParams, triggerParams);

    }

    private Map<String, List<String>> getRefererParameters(Map<String, String[]> requestParams, String referer) {

        Map<String, List<String>> result = new HashMap<>();

        try {
            for (Map.Entry<String, String> pair : getQueryParams(new URI(referer))) {
                if (!requestParams.containsKey(pair.getKey())) {
                    if (result.containsKey(pair.getKey())) {
                        List<String> values = result.get(pair.getKey());
                        values.add(pair.getValue());
                        result.put(pair.getKey(), values);
                    } else {
                        result.put(pair.getKey(), new ArrayList<>(Collections.singletonList(pair.getValue())));
                    }
                }
            }
        } catch (URISyntaxException e) {
            log.warn("Ignoring invalid referer: " + referer);
        }

        return result;

    }

    private List<Map.Entry<String, String>> getQueryParams(URI uri) {
        try {
            List<Map.Entry<String, String>> result = new ArrayList<>();
            if (Strings.isNotBlank(uri.getQuery())) {
                for (String pair : uri.getQuery().split("&")) {
                    int idx = pair.indexOf("=");
                    String key = idx > 0 ?
                        URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                    String value = idx > 0 && pair.length() > idx + 1 ?
                        URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
                    result.add(new AbstractMap.SimpleEntry<>(key, value));
                }
            }
            return result;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isTriggerParamPresent(Map<String, String[]> actualParams, String[] triggerParams) {
        for (String triggerParam : triggerParams) {
            String[] paramData = SPLIT_ON_EQUALS.split(triggerParam, 2);
            if (actualParams.containsKey(paramData[0])) {
                if (paramData.length == 1) {
                    return true;
                }
                String[] paramValues = actualParams.get(paramData[0]);
                if (paramValues != null) {
                    for (String paramValue : paramValues) {
                        if (Objects.equals(paramData[1], paramValue)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[]{
                ParameterBuilder.create(PARAM_URL_PARAMS)
                        .label("Query Parameters")
                        .description("A comma-separated list of Name[=Value] pairs for which the feature " +
                        "should be active. Please choose unique names for your parameter(s). " +
                        "If no value is specified, simply having the parameter present will activate the toggle.\n" +
                        "Important Note: Use unique parameter key/value pairs to avoid accidental activation.")
        };
    }
}
