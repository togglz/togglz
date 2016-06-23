package org.togglz.spring.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

public class FeatureInterceptorTest {

    private static final int METHOD_FEATURE_TWO_RESPONSE_STATUS = 302;
    
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

    @FeaturesAreActive(featureClass = TestFeatures.class, features = "CLASS_FEATURE")
    private static class TestController {
        @SuppressWarnings("unused")
        public void classFeature() { }
        
        @FeaturesAreActive(featureClass = TestFeatures.class, features = "METHOD_FEATURE")
        public void methodFeature() { }
        
        @FeaturesAreActive(featureClass = TestFeatures.class, features = {"METHOD_FEATURE", "METHOD_FEATURE_TWO"}, responseStatus = METHOD_FEATURE_TWO_RESPONSE_STATUS)
        public void methodFeatureTwo() { }
        
        @FeaturesAreActive(featureClass = NoEnumFeature.class, features = "A")
        public void methodFeatureNoEnum() { }
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
        assertPrehandle("classFeature", false, HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void preHandle_ClassFeature_Active() throws Exception {
        enableFeature(TestFeatures.CLASS_FEATURE);
        assertPrehandle("classFeature", true, HttpStatus.OK.value());
    }

    @Test
    public void preHandle_MethodFeature_Inactive() throws Exception {
        assertPrehandle("methodFeature", false, HttpStatus.NOT_FOUND.value());
    }
    
    @Test
    public void preHandle_MethodFeature_Active() throws Exception {
        enableFeature(TestFeatures.METHOD_FEATURE);
        assertPrehandle("methodFeature", true, HttpStatus.OK.value());
    }

    @Test
    public void preHandle_MethodFeatureTwo_Inactive() throws Exception {
        assertPrehandle("methodFeatureTwo", false, METHOD_FEATURE_TWO_RESPONSE_STATUS);
    }
    
    @Test
    public void preHandle_MethodFeatureTwo_OnlyOneActive() throws Exception {
        enableFeature(TestFeatures.METHOD_FEATURE);
        assertPrehandle("methodFeatureTwo", false, METHOD_FEATURE_TWO_RESPONSE_STATUS);
    }

    @Test
    public void preHandle_MethodFeatureTwo_AllActive() throws Exception {
        enableFeature(TestFeatures.METHOD_FEATURE);
        enableFeature(TestFeatures.METHOD_FEATURE_TWO);
        assertPrehandle("methodFeatureTwo", true, HttpStatus.OK.value());
    }

    @Test(expected = IllegalArgumentException.class)
    public void preHandle_methodFeatureNoEnum_InvalidEnum() throws Exception {
        assertPrehandle("methodFeatureNoEnum", false, HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void handlerAnnotation_OnType() throws NoSuchMethodException {
        TestController controller = new TestController();
        HandlerMethod handler = new HandlerMethod(controller, "classFeature");
        FeaturesAreActive annotation = FeatureInterceptor.handlerAnnotation(handler, FeaturesAreActive.class);
        assertNotNull(annotation);
        assertEquals(TestFeatures.class, annotation.featureClass());
        assertEquals(HttpStatus.NOT_FOUND.value(), annotation.responseStatus());
        assertThat(annotation.features()).containsExactly("CLASS_FEATURE");
    }

    @Test
    public void handlerAnnotation_OnMethod() throws NoSuchMethodException {
        TestController controller = new TestController();
        HandlerMethod handler = new HandlerMethod(controller, "methodFeatureTwo");
        FeaturesAreActive annotation = FeatureInterceptor.handlerAnnotation(handler, FeaturesAreActive.class);
        assertNotNull(annotation);
        assertEquals(TestFeatures.class, annotation.featureClass());
        assertEquals(302, annotation.responseStatus());
        assertThat(annotation.features()).containsExactly("METHOD_FEATURE", "METHOD_FEATURE_TWO");
    }

    @Test
    public void enumFrom() {
        assertEquals(TestFeatures.CLASS_FEATURE, FeatureInterceptor.enumFrom("CLASS_FEATURE", TestFeatures.class));
        assertEquals(TestFeatures.METHOD_FEATURE, FeatureInterceptor.enumFrom("METHOD_FEATURE", TestFeatures.class));
        assertNull(FeatureInterceptor.enumFrom("FOO", TestFeatures.class));
        assertNull(FeatureInterceptor.enumFrom(null, TestFeatures.class));
    }
   
    private void enableFeature(TestFeatures feature) {
        repository.setFeatureState(new FeatureState(feature, true));
        assertTrue(manager.isActive(feature));
    }

    private void assertPrehandle(String methodName, boolean expectedReturnValue, int expectedStatusCode) throws NoSuchMethodException, Exception {
        FeatureInterceptor featureInterceptor = new FeatureInterceptor();
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        TestController controller = new TestController();
        HandlerMethod handler = new HandlerMethod(controller, methodName);
        
        assertEquals(expectedReturnValue, featureInterceptor.preHandle(request, response, handler));
        assertEquals(expectedStatusCode, response.getStatus());
    }
}


