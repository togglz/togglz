package org.togglz.servlet.activation;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

import static org.mockito.Mockito.when;

public class UrlParameterActivationStrategyTest {
    private FeatureUser user;
    private FeatureState state;
    private HttpServletRequest request;
    private final UrlParameterActivationStrategy strategy = new UrlParameterActivationStrategy();

    @Before
    public void setUp() {
        user = new SimpleFeatureUser("ea", false);
        state = new FeatureState(MyFeature.FEATURE).enable();
        state.setParameter(UrlParameterActivationStrategy.PARAM_URL_PARAMS,
            "toggleFeatureX=true,toggleAll=yes,parameterWithoutValue");
        request = Mockito.mock(HttpServletRequest.class);
        HttpServletRequestHolder.bind(request);
    }

    @After
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

        Assert.assertFalse(isActive);
    }

    @Test
    public void shouldNotBeActiveWithNoPermittedParameters() {
        when(request.getParameterMap()).thenReturn(Collections.EMPTY_MAP);

        boolean isActive = strategy.isActive(state, user);

        Assert.assertFalse(isActive);
    }

    @Test
    public void shouldNotBeActiveWhenOnlyNonMatchingParametersArePresent() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("somethingThatDoesNotMatch", (String[]) Arrays.asList("true").toArray());
        parameters.put("somethingElse", (String[]) Arrays.asList("aValue").toArray());
        when(request.getParameterMap()).thenReturn(parameters);

        boolean isActive = strategy.isActive(state, user);

        Assert.assertFalse(isActive);
    }

    @Test
    public void shouldNotBeActiveWhenMatchingParameterHasANonMatchingValue() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("toggleFeatureX", (String[]) Arrays.asList("false").toArray());
        parameters.put("toggleAll", (String[]) Arrays.asList("nope").toArray());
        when(request.getParameterMap()).thenReturn(parameters);

        boolean isActive = strategy.isActive(state, user);

        Assert.assertFalse(isActive);
    }

    @Test
    public void shouldNotBeActiveWhenMatchingParameterRequiringValueDoesNotHaveValue() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("toggleFeatureX", null);
        when(request.getParameterMap()).thenReturn(parameters);

        boolean isActive = strategy.isActive(state, user);

        Assert.assertFalse(isActive);
    }

    @Test
    public void shouldBeActiveWhenAnAcceptedParameterAndValueArePresent() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("toggleFeatureX", (String[]) Arrays.asList("true").toArray());
        parameters.put("toggleAll", (String[]) Arrays.asList("nope").toArray());
        when(request.getParameterMap()).thenReturn(parameters);

        boolean isActive = strategy.isActive(state, user);

        Assert.assertTrue(isActive);
    }

    @Test
    public void shouldBeActiveWhenAnAcceptedParameterWithoutRequiredValueIsPresent() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("parameterWithoutValue", null);
        when(request.getParameterMap()).thenReturn(parameters);

        boolean isActive = strategy.isActive(state, user);

        Assert.assertTrue(isActive);
    }

    @Test
    public void shouldBeActiveWhenAnAcceptedParameterWithoutRequiredValueIsPresentAndItHasAValue() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("parameterWithoutValue", (String[]) Arrays.asList("anyVal").toArray());
        when(request.getParameterMap()).thenReturn(parameters);

        boolean isActive = strategy.isActive(state, user);

        Assert.assertTrue(isActive);
    }

    @Test
    public void shouldBeActiveRegardlessOfWhitespaceAroundActivationParams() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("parameterWithoutValue", null);
        parameters.put("anotherParam", null);
        when(request.getParameterMap()).thenReturn(parameters);

        state.setParameter(UrlParameterActivationStrategy.PARAM_URL_PARAMS,
            " toggleFeatureX  = true, toggleAll= yes, parameterWithoutValue ");
        boolean isActive = strategy.isActive(state, user);
        Assert.assertTrue(isActive);
    }

    @Test
    public void shouldBeActiveIfParameterWithMultipleValuesHasAnyMatch() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("parameterWithoutValue", null);
        parameters.put("anotherParam", null);
        when(request.getParameterMap()).thenReturn(parameters);

        parameters.put("test", (String[]) Arrays.asList("matches", "nomatch", "notanything").toArray());
        state.setParameter(UrlParameterActivationStrategy.PARAM_URL_PARAMS, "test=matches");

        boolean isActive = strategy.isActive(state, user);

        Assert.assertTrue(isActive);
    }

    @Test
    public void strategyShouldCheckParametersOfReferrer() {
        Map<String, String[]> parameters = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameters);

        when(request.getHeader("referer")).thenReturn("http://mysite.com/?parameterWithoutValue");
        boolean isActive = strategy.isActive(state, user);
        Assert.assertTrue(isActive);

        when(request.getHeader("referer")).thenReturn("http://mysite.com/?someOtherParameter");
        isActive = strategy.isActive(state, user);
        Assert.assertFalse(isActive);

        when(request.getHeader("referer")).thenReturn("http://mysite.com/?toggleFeatureX=true");
        isActive = strategy.isActive(state, user);
        Assert.assertTrue(isActive);
    }

    @Test
    public void refererParamsShouldNotOverrideRequestParamsButWillQualifyAsActiveIfSeparate() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("toggleFeatureX", (String[]) Arrays.asList("false").toArray());
        when(request.getParameterMap()).thenReturn(parameters);

        when(request.getHeader("referer")).thenReturn("http://mysite.com/?toggleFeatureX=true");
        boolean isActive = strategy.isActive(state, user);
        Assert.assertFalse(isActive);

        when(request.getHeader("referer")).thenReturn("http://mysite.com/?parameterWithoutValue");
        isActive = strategy.isActive(state, user);
        Assert.assertTrue(isActive);
    }

    @Test
    public void refererWithMultiValueParamsShouldBeChecked() {
        Map<String, String[]> parameters = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameters);

        when(request.getHeader("referer"))
            .thenReturn("http://mysite.com/?toggleFeatureX=false&toggleFeatureX=nope&toggleFeatureX=true");
        boolean isActive = strategy.isActive(state, user);
        Assert.assertTrue(isActive);
    }

    @Test
    public void refererWithEncodedParamsCanMatch() {
        Map<String, String[]> parameters = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameters);
        state.setParameter(UrlParameterActivationStrategy.PARAM_URL_PARAMS,
            "toggleFeatureX=handles space,toggleAll=yes,parameterWithoutValue");

        when(request.getHeader("referer"))
            .thenReturn("http://mysite.com/?toggleFeatureX=handles%20space&toggleFeatureX=a%22thing");
        boolean isActive = strategy.isActive(state, user);
        Assert.assertTrue(isActive);
    }
}
