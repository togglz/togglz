package org.togglz.servlet.activation;

import org.togglz.core.activation.ParameterBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.togglz.core.activation.Parameter;
import org.togglz.core.util.Strings;
import org.togglz.servlet.util.HttpServletRequestHolder;
import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;

public class UrlParameterActivationStrategy implements ActivationStrategy {
    private final Log log = LogFactory.getLog(UrlParameterActivationStrategy.class);
    public static final String PARAM_URL_PARAMS = "params";
    private static final Pattern SPLIT_ON_COMMA = Pattern.compile("\\s*,\\s*");
    private static final Pattern SPLIT_ON_EQUALS = Pattern.compile("\\s*=\\s*");

    @Override
    public String getId() {
        return "url-parameter";
    }

    @Override
    public String getName() {
        return "URL Parameter";
    }

    @Override
    public boolean isActive(FeatureState featureState, FeatureUser user) {
        HttpServletRequest request = HttpServletRequestHolder.get();
        if (request == null) {
            return false;
        }

        String allowedUrlParams = featureState.getParameter(PARAM_URL_PARAMS);
        if (Strings.isBlank(allowedUrlParams)) {
            return false;
        }

        //Parse the request params and activation params and compare them for a match
        String[] allowedParams = SPLIT_ON_COMMA.split(allowedUrlParams.trim());
        Map<String, String[]> requestParams = new HashMap<>();
        requestParams.putAll(request.getParameterMap());
        String referer = request.getHeader("referer");
        if (referer != null) {
            try {
                Map<String, List<String>> combinedRefererParameters = getRefererParameters(requestParams, referer);

                for (Map.Entry<String, List<String>> entry : combinedRefererParameters.entrySet()) {
                    List<String> val = entry.getValue();
                    String[] values = new String[val.size()];
                    requestParams.put(entry.getKey(), val.toArray(values));
                }
            } catch (Exception e) {
                log.error("Error parsing referer for Togglz parameter activation", e);
            }
        }

        return hasActivationUrlParameter(allowedParams, requestParams);
    }

    private Map<String, List<String>> getRefererParameters(Map<String, String[]> requestParams, String referer)
        throws URISyntaxException, UnsupportedEncodingException {
        List<Map.Entry<String, String>> queryPairs = getParameterNameValuePairsFromURL(referer);

        Map<String, List<String>> combinedRefererParameters = new HashMap<>();
        for (Map.Entry<String, String> pair : queryPairs) {
            if (!requestParams.containsKey(pair.getKey())) {
                if (combinedRefererParameters.containsKey(pair.getKey())) {
                    List<String> values = combinedRefererParameters.get(pair.getKey());
                    values.add(pair.getValue());
                    combinedRefererParameters.put(pair.getKey(), values);
                } else {
                    combinedRefererParameters.put(pair.getKey(), new ArrayList<>(Arrays.asList(pair.getValue())));
                }
            }
        }
        return combinedRefererParameters;
    }

    private List<Map.Entry<String, String>> getParameterNameValuePairsFromURL(String referer)
        throws URISyntaxException, UnsupportedEncodingException {
        List<Map.Entry<String, String>> queryPairs = new ArrayList<>();
        URI uri = new URI(referer);
        String[] pairs = uri.getQuery().split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            final String value =
                idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            queryPairs.add(new AbstractMap.SimpleEntry<>(key, value));

        }
        return queryPairs;
    }

    private boolean hasActivationUrlParameter(String[] allowedParams, Map<String, String[]> requestParams) {
        for (String allowedParam : allowedParams) {
            String[] paramData = SPLIT_ON_EQUALS.split(allowedParam, 2);
            if (requestParams.containsKey(paramData[0])) {
                if (paramData.length == 1) {
                    return true;
                }
                String[] paramValues = requestParams.get(paramData[0]);
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
        return new Parameter[] {
            ParameterBuilder.create(PARAM_URL_PARAMS).label("URL Parameters")
                .description(
                "A comma-separated list of Name[=Value] pairs for which the feature should be active. Please choose unique names for your parameter(s). If no value is specified, simply having the parameter present will activate the toggle. \nImportant Note: Use unique parameter key/value pairs to avoid accidental activation.")
        };
    }
}
