package org.togglz.servlet.activation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.servlet.util.HttpServletRequestHolder;

import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class QueryParameterActivationStrategyTest {
    private FeatureUser user;
    private FeatureState state;
    private HttpServletRequest request;
    private final QueryParameterActivationStrategy strategy = new QueryParameterActivationStrategy();

    @BeforeEach
    public void setUp() {
        user = new SimpleFeatureUser("ea", false);
        state = new FeatureState(MyFeature.FEATURE).enable();
        state.setParameter(QueryParameterActivationStrategy.PARAM_URL_PARAMS,
            "toggleFeatureX=true,toggleAll=yes,parameterWithoutValue");
        request = Mockito.mock(HttpServletRequest.class);
        HttpServletRequestHolder.bind(request);
    }

    @AfterEach
    public void cleanup() {
        HttpServletRequestHolder.release();
    }

    private enum MyFeature implements Feature {
        FEATURE
    }

    @Test
    public void shouldNotBeActiveWithANullRequestObject() {
        HttpServletRequestHolder.release();

        boolean isActive = strategy.isActive(state, user);

        assertFalse(isActive);
    }

    @Test
    public void shouldNotBeActiveWithNoPermittedParameters() {
        when(request.getParameterMap()).thenReturn(Collections.EMPTY_MAP);

        boolean isActive = strategy.isActive(state, user);

        assertFalse(isActive);
    }

    @Test
    public void shouldNotBeActiveWhenOnlyNonMatchingParametersArePresent() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("somethingThatDoesNotMatch", new String[]{"true"});
        parameters.put("somethingElse", new String[]{"aValue"});

        when(request.getParameterMap()).thenReturn(parameters);

        boolean isActive = strategy.isActive(state, user);

        assertFalse(isActive);
    }

    @Test
    public void shouldNotBeActiveWhenMatchingParameterHasANonMatchingValue() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("toggleFeatureX", new String[]{"false"});
        parameters.put("toggleAll", new String[]{"nope"});
        when(request.getParameterMap()).thenReturn(parameters);

        boolean isActive = strategy.isActive(state, user);

        assertFalse(isActive);
    }

    @Test
    public void shouldNotBeActiveWhenMatchingParameterRequiringValueDoesNotHaveValue() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("toggleFeatureX", null);
        when(request.getParameterMap()).thenReturn(parameters);

        boolean isActive = strategy.isActive(state, user);

        assertFalse(isActive);
    }

    @Test
    public void shouldBeActiveWhenAnAcceptedParameterAndValueArePresent() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("toggleFeatureX", new String[]{"true"});
        parameters.put("toggleAll", new String[]{"nope"});
        when(request.getParameterMap()).thenReturn(parameters);

        boolean isActive = strategy.isActive(state, user);

        assertTrue(isActive);
    }

    @Test
    public void shouldBeActiveWhenAnAcceptedParameterWithoutRequiredValueIsPresent() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("parameterWithoutValue", null);
        when(request.getParameterMap()).thenReturn(parameters);

        boolean isActive = strategy.isActive(state, user);

        assertTrue(isActive);
    }

    @Test
    public void shouldBeActiveWhenAnAcceptedParameterWithoutRequiredValueIsPresentAndItHasAValue() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("parameterWithoutValue", new String[]{"anyVal"});
        when(request.getParameterMap()).thenReturn(parameters);

        boolean isActive = strategy.isActive(state, user);

        assertTrue(isActive);
    }

    @Test
    public void shouldBeActiveRegardlessOfWhitespaceAroundActivationParams() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("parameterWithoutValue", null);
        parameters.put("anotherParam", null);
        when(request.getParameterMap()).thenReturn(parameters);

        state.setParameter(QueryParameterActivationStrategy.PARAM_URL_PARAMS,
            " toggleFeatureX  = true, toggleAll= yes, parameterWithoutValue ");
        boolean isActive = strategy.isActive(state, user);
        assertTrue(isActive);
    }

    @Test
    public void shouldBeActiveIfParameterWithMultipleValuesHasAnyMatch() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("parameterWithoutValue", null);
        parameters.put("anotherParam", null);
        when(request.getParameterMap()).thenReturn(parameters);

        parameters.put("test", new String[]{"matches", "nomatch", "notanything"});
        state.setParameter(QueryParameterActivationStrategy.PARAM_URL_PARAMS, "test=matches");

        boolean isActive = strategy.isActive(state, user);

        assertTrue(isActive);
    }

    @Test
    public void strategyShouldCheckParametersOfReferrer() {
        Map<String, String[]> parameters = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameters);

        when(request.getHeader("referer")).thenReturn("http://mysite.com/?parameterWithoutValue");
        boolean isActive = strategy.isActive(state, user);
        assertTrue(isActive);

        when(request.getHeader("referer")).thenReturn("http://mysite.com/?someOtherParameter");
        isActive = strategy.isActive(state, user);
        assertFalse(isActive);

        when(request.getHeader("referer")).thenReturn("http://mysite.com/?toggleFeatureX=true");
        isActive = strategy.isActive(state, user);
        assertTrue(isActive);
    }

    @Test
    public void refererParamsShouldNotOverrideRequestParamsButWillQualifyAsActiveIfSeparate() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("toggleFeatureX", new String[]{"false"});
        when(request.getParameterMap()).thenReturn(parameters);

        when(request.getHeader("referer")).thenReturn("http://mysite.com/?toggleFeatureX=true");
        boolean isActive = strategy.isActive(state, user);
        assertFalse(isActive);

        when(request.getHeader("referer")).thenReturn("http://mysite.com/?parameterWithoutValue");
        isActive = strategy.isActive(state, user);
        assertTrue(isActive);
    }

    @Test
    public void refererWithMultiValueParamsShouldBeChecked() {
        Map<String, String[]> parameters = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameters);

        when(request.getHeader("referer"))
            .thenReturn("http://mysite.com/?toggleFeatureX=false&toggleFeatureX=nope&toggleFeatureX=true");
        boolean isActive = strategy.isActive(state, user);
        assertTrue(isActive);
    }

    @Test
    public void refererWithEncodedParamsCanMatch() {
        Map<String, String[]> parameters = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameters);
        state.setParameter(QueryParameterActivationStrategy.PARAM_URL_PARAMS,
            "toggleFeatureX=handles space,toggleAll=yes,parameterWithoutValue");

        when(request.getHeader("referer"))
            .thenReturn("http://mysite.com/?toggleFeatureX=handles%20space&toggleFeatureX=a%22thing");
        boolean isActive = strategy.isActive(state, user);
        assertTrue(isActive);
    }

    @Test
    public void refererWithoutQueryParam() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("parameterWithoutValue", new String[0]);
        when(request.getParameterMap()).thenReturn(parameters);
        state.setParameter(QueryParameterActivationStrategy.PARAM_URL_PARAMS,
            "toggleFeatureX=handles space,toggleAll=yes,parameterWithoutValue");

        when(request.getHeader("referer"))
            .thenReturn("http://mysite.com/");
        boolean isActive = strategy.isActive(state, user);
        assertTrue(isActive);
    }

}
