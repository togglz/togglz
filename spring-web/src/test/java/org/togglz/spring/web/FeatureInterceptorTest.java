package org.togglz.spring.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.context.ThreadLocalFeatureManagerProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.mem.InMemoryStateRepository;

/**
 * @author ractive
 * @author m-schroeer
 */
public class FeatureInterceptorTest {

    private static final HttpStatus DEFAULT_ERROR_RESPONSE_STATUS = FeaturesAreActive.DEFAULT_ERROR_RESPONSE_STATUS;

    private FeatureManager manager;
    private InMemoryStateRepository repository;

    private static enum TestFeatures implements Feature {
        CLASS_FEATURE,
        METHOD_FEATURE,
        METHOD_FEATURE_TWO
    }
    
    private static class NoEnumFeature implements Feature {
        @Override
        public String name() {
            return "johndoe";
        }}

    @Before
    public void before() {
        repository = new InMemoryStateRepository();
        manager = new FeatureManagerBuilder()
            .featureEnum(TestFeatures.class)
            .stateRepository(repository)
            .build();
        
        ThreadLocalFeatureManagerProvider.bind(manager);
    }

    @After
    public void after() {
        ThreadLocalFeatureManagerProvider.release();
        FeatureContext.clearCache();
    }

    @FeaturesAreActive(features = "CLASS_FEATURE")
    private static class TestController {
        @SuppressWarnings("unused")
        public void classFeature() { }
        
        @FeaturesAreActive(features = "METHOD_FEATURE")
        public void methodFeature() { }

        @FeaturesAreActive(features = {"METHOD_FEATURE", "METHOD_FEATURE_TWO"}, errorResponseStatus = HttpStatus.FOUND)
        public void methodFeatureTwo() { }
        
        @FeaturesAreActive(features = {"METHOD_FEATURE", "NO_FEATURE", "METHOD_FEATURE_TWO"})
        public void methodFeatureAtLeastOneIsNoFeature() { }

        @FeaturesAreActive(features = "METHOD_FEATURE", responseStatus = 402)
        public void methodFeatureDeprecatedResponseStatusNotDefault() { }

        @FeaturesAreActive(features = "METHOD_FEATURE", errorResponseStatus = HttpStatus.FORBIDDEN)
        public void methodFeatureErrorResponseStatusNotDefault() { }

        @FeaturesAreActive(features = "METHOD_FEATURE", responseStatus = 403, errorResponseStatus = HttpStatus.FORBIDDEN)
        public void methodFeatureDeprecatedResponseAndErrorResponseEqualValue() { }

        @FeaturesAreActive(features = "METHOD_FEATURE", responseStatus = 402, errorResponseStatus = HttpStatus.FORBIDDEN)
        public void methodFeatureDeprecatedResponseAndErrorResponseDifferentValues() { }
    }

    private static class NonAnnotatedTestController {
        @SuppressWarnings("unused")
        public void doit() { }
    }

    @Test
    public void preHandle_noAnnotations() throws Exception {
        FeatureInterceptor featureInterceptor = new FeatureInterceptor();
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        NonAnnotatedTestController controller = new NonAnnotatedTestController();
        HandlerMethod handler = new HandlerMethod(controller, "doit");
        
        assertEquals(true, featureInterceptor.preHandle(request, response, handler));
        assertEquals(200, response.getStatus());
    }

    @Test
    public void preHandle_ClassFeature_Inactive() throws Exception {
        assertPrehandle("classFeature", false, DEFAULT_ERROR_RESPONSE_STATUS);
    }

    @Test
    public void preHandle_ClassFeature_Active() throws Exception {
        enableFeature(TestFeatures.CLASS_FEATURE);
        assertPrehandle("classFeature", true, HttpStatus.OK);
    }

    @Test
    public void preHandle_MethodFeature_Inactive() throws Exception {
        assertPrehandle("methodFeature", false, DEFAULT_ERROR_RESPONSE_STATUS);
    }
    
    @Test
    public void preHandle_MethodFeature_Active() throws Exception {
        enableFeature(TestFeatures.METHOD_FEATURE);
        assertPrehandle("methodFeature", true, HttpStatus.OK);
    }

    @Test
    public void preHandle_MethodFeatureTwo_Inactive() throws Exception {
        assertPrehandle("methodFeatureTwo", false, HttpStatus.FOUND);
    }
    
    @Test
    public void preHandle_MethodFeatureTwo_OnlyOneActive() throws Exception {
        enableFeature(TestFeatures.METHOD_FEATURE);
        assertPrehandle("methodFeatureTwo", false, HttpStatus.FOUND);
    }

    @Test
    public void preHandle_MethodFeatureTwo_AllActive() throws Exception {
        enableFeature(TestFeatures.METHOD_FEATURE);
        enableFeature(TestFeatures.METHOD_FEATURE_TWO);
        assertPrehandle("methodFeatureTwo", true, HttpStatus.OK);
    }

    @Test(expected = IllegalArgumentException.class)
    public void preHandle_MethodFeatureAtLeastOneIsNoFeature_AllActualFeaturesActive() throws Exception {
        enableFeature(TestFeatures.METHOD_FEATURE);
        enableFeature(TestFeatures.METHOD_FEATURE_TWO);
        assertPrehandle("methodFeatureAtLeastOneIsNoFeature", false, DEFAULT_ERROR_RESPONSE_STATUS);
    }

    @Test
    public void preHandle_MethodFeature_preferDeprecatedResponseIfNotDefault() throws Exception {
        assertPrehandle("methodFeatureDeprecatedResponseStatusNotDefault", false, HttpStatus.valueOf(402));
    }

    @Test
    public void preHandle_MethodFeature_preferErrorResponseStatusIfNotDefault() throws Exception {
        assertPrehandle("methodFeatureErrorResponseStatusNotDefault", false, HttpStatus.FORBIDDEN);
    }

    @Test
    public void preHandle_MethodFeature_useAnyIfDeprecatedResponseStatusAndErrorResponseStatusAreEqual() throws Exception {
        assertPrehandle("methodFeatureDeprecatedResponseAndErrorResponseEqualValue", false, HttpStatus.FORBIDDEN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void preHandle_MethodFeature_throwISEIfDeprecatedResponseStatusAndErrorResponseStatusAreDifferent() throws Exception {
        // just to compile. the actual value is irrelevant, as there should be an exception because of ambiguous configuration
        final HttpStatus any = HttpStatus.valueOf(200);
        assertPrehandle("methodFeatureDeprecatedResponseAndErrorResponseDifferentValues", false, any);
    }

    @Test
    public void handlerAnnotation_OnType() throws NoSuchMethodException {
        TestController controller = new TestController();
        HandlerMethod handler = new HandlerMethod(controller, "classFeature");
        FeaturesAreActive annotation = FeatureInterceptor.handlerAnnotation(handler, FeaturesAreActive.class);
        assertNotNull(annotation);
        assertEquals(DEFAULT_ERROR_RESPONSE_STATUS, annotation.errorResponseStatus());
        assertThat(annotation.features()).containsExactly("CLASS_FEATURE");
    }

    @Test
    public void handlerAnnotation_OnMethod() throws NoSuchMethodException {
        TestController controller = new TestController();
        HandlerMethod handler = new HandlerMethod(controller, "methodFeatureTwo");
        FeaturesAreActive annotation = FeatureInterceptor.handlerAnnotation(handler, FeaturesAreActive.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.FOUND, annotation.errorResponseStatus());
        assertThat(annotation.features()).containsExactly("METHOD_FEATURE", "METHOD_FEATURE_TWO");
    }

    private void enableFeature(TestFeatures feature) {
        repository.setFeatureState(new FeatureState(feature, true));
        assertTrue(manager.isActive(feature));
    }

    private void assertPrehandle(String methodName, boolean expectedReturnValue, HttpStatus expectedStatus) throws NoSuchMethodException, Exception {
        FeatureInterceptor featureInterceptor = new FeatureInterceptor();
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        TestController controller = new TestController();
        HandlerMethod handler = new HandlerMethod(controller, methodName);
        
        assertEquals(expectedReturnValue, featureInterceptor.preHandle(request, response, handler));
        assertEquals(expectedStatus.value(), response.getStatus());
    }
}


