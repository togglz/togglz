package org.togglz.servlet.activation;

import org.togglz.core.activation.ParameterBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.togglz.core.activation.Parameter;
import org.togglz.core.util.Strings;
import org.togglz.servlet.util.HttpServletRequestHolder;
import javax.servlet.http.HttpServletRequest;

public class UrlParameterActivationStrategy implements ActivationStrategy {
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
					List<String> val  = entry.getValue();
					String[] values = new String[val.size()];
					requestParams.put(entry.getKey(), val.toArray(values));
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}

		}

		return hasActivationUrlParameter(allowedParams, requestParams);
	}

	private Map<String, List<String>> getRefererParameters(Map<String, String[]> requestParams, String referer) throws URISyntaxException {
		List<NameValuePair> refererParameters = new URIBuilder(referer).getQueryParams();
		Map<String, List<String>> combinedRefererParameters = new HashMap<>();
		for (NameValuePair pair : refererParameters) {
			if (!requestParams.containsKey(pair.getName())) {
				if (combinedRefererParameters.containsKey(pair.getName())) {
					List<String> values = combinedRefererParameters.get(pair.getName());
					values.add(pair.getValue());
					combinedRefererParameters.put(pair.getName(), values);
				} else {
					combinedRefererParameters.put(pair.getName(), new ArrayList<>(Arrays.asList(pair.getValue())));
				}
			}
		}
		return combinedRefererParameters;
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
		return new Parameter[]{
				ParameterBuilder.create(PARAM_URL_PARAMS).label("URL Parameters")
						.description("A comma-separated list of Name[=Value] pairs for which the feature should be active. Please choose unique names for your paramter(s). If no value is specified, simply having the parameter present will activate the toggle. \nImportant Note: Use unique parameter key/value pairs to avoid accidental activation.")
		};
	}
}
