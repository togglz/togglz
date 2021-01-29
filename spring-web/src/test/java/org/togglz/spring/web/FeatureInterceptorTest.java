package org.togglz.spring.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author ractive
 * @author m-schroeer
 */
class FeatureInterceptorTest {

    private static final HttpStatus DEFAULT_ERROR_RESPONSE_STATUS = FeaturesAreActive.DEFAULT_ERROR_RESPONSE_STATUS;

    private FeatureManager manager;
    private InMemoryStateRepository repository;

    private enum TestFeatures implements Feature {
        CLASS_FEATURE,
        METHOD_FEATURE,
        METHOD_FEATURE_TWO
    }

    @BeforeEach
    void before() {
        this.repository = new InMemoryStateRepository();
        this.manager = new FeatureManagerBuilder()
                .featureEnum(TestFeatures.class)
                .stateRepository(this.repository)
                .build();

        ThreadLocalFeatureManagerProvider.bind(this.manager);
    }

    @AfterEach
    void after() {
        ThreadLocalFeatureManagerProvider.release();
        FeatureContext.clearCache();
    }

    @FeaturesAreActive(features = "CLASS_FEATURE")
    private static class TestController {

        @SuppressWarnings("unused")
        public void classFeature() {
        }

        @FeaturesAreActive(features = "METHOD_FEATURE")
        public void methodFeature() {
        }

        @FeaturesAreActive(features = {"METHOD_FEATURE", "METHOD_FEATURE_TWO"}, errorResponseStatus = HttpStatus.FOUND)
        public void methodFeatureTwo() {
        }

        @FeaturesAreActive(features = {"METHOD_FEATURE", "NO_FEATURE", "METHOD_FEATURE_TWO"})
        public void methodFeatureAtLeastOneIsNoFeature() {
        }

        @FeaturesAreActive(features = "METHOD_FEATURE", responseStatus = 402)
        public void methodFeatureDeprecatedResponseStatusNotDefault() {
        }

        @FeaturesAreActive(features = "METHOD_FEATURE", errorResponseStatus = HttpStatus.FORBIDDEN)
        public void methodFeatureErrorResponseStatusNotDefault() {
        }

        @FeaturesAreActive(features = "METHOD_FEATURE", responseStatus = 403, errorResponseStatus = HttpStatus.FORBIDDEN)
        public void methodFeatureDeprecatedResponseAndErrorResponseEqualValue() {
        }

        @FeaturesAreActive(features = "METHOD_FEATURE", responseStatus = 402, errorResponseStatus = HttpStatus.FORBIDDEN)
        public void methodFeatureDeprecatedResponseAndErrorResponseDifferentValues() {
        }
    }

    private static class NonAnnotatedTestController {

        @SuppressWarnings("unused")
        public void doit() {
        }
    }

    @Test
    void preHandle_noAnnotations() throws Exception {
        final FeatureInterceptor featureInterceptor = new FeatureInterceptor();

        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final NonAnnotatedTestController controller = new NonAnnotatedTestController();
        final HandlerMethod handler = new HandlerMethod(controller, "doit");

        assertTrue(featureInterceptor.preHandle(request, response, handler));
        assertEquals(200, response.getStatus());
    }

    @Test
    void preHandle_ClassFeature_Inactive() throws Exception {
        assertPreHandle("classFeature", false, DEFAULT_ERROR_RESPONSE_STATUS);
    }

    @Test
    void preHandle_ClassFeature_Active() throws Exception {
        enableFeature(TestFeatures.CLASS_FEATURE);
        assertPreHandle("classFeature", true, HttpStatus.OK);
    }

    @Test
    void preHandle_MethodFeature_Inactive() throws Exception {
        assertPreHandle("methodFeature", false, DEFAULT_ERROR_RESPONSE_STATUS);
    }

    @Test
    void preHandle_MethodFeature_Active() throws Exception {
        enableFeature(TestFeatures.METHOD_FEATURE);
        assertPreHandle("methodFeature", true, HttpStatus.OK);
    }

    @Test
    void preHandle_MethodFeatureTwo_Inactive() throws Exception {
        assertPreHandle("methodFeatureTwo", false, HttpStatus.FOUND);
    }

    @Test
    void preHandle_MethodFeatureTwo_OnlyOneActive() throws Exception {
        enableFeature(TestFeatures.METHOD_FEATURE);
        assertPreHandle("methodFeatureTwo", false, HttpStatus.FOUND);
    }

    @Test
    void preHandle_MethodFeatureTwo_AllActive() throws Exception {
        enableFeature(TestFeatures.METHOD_FEATURE);
        enableFeature(TestFeatures.METHOD_FEATURE_TWO);
        assertPreHandle("methodFeatureTwo", true, HttpStatus.OK);
    }

    @Test
    void preHandle_MethodFeatureAtLeastOneIsNoFeature_AllActualFeaturesActive() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            enableFeature(TestFeatures.METHOD_FEATURE);
            enableFeature(TestFeatures.METHOD_FEATURE_TWO);
            assertPreHandle("methodFeatureAtLeastOneIsNoFeature", false, DEFAULT_ERROR_RESPONSE_STATUS);
        });
    }

    @Test
    void preHandle_MethodFeature_preferDeprecatedResponseIfNotDefault() throws Exception {
        assertPreHandle("methodFeatureDeprecatedResponseStatusNotDefault", false, HttpStatus.valueOf(402));
    }

    @Test
    void preHandle_MethodFeature_preferErrorResponseStatusIfNotDefault() throws Exception {
        assertPreHandle("methodFeatureErrorResponseStatusNotDefault", false, HttpStatus.FORBIDDEN);
    }

    @Test
    void preHandle_MethodFeature_useAnyIfDeprecatedResponseStatusAndErrorResponseStatusAreEqual() throws Exception {
        assertPreHandle("methodFeatureDeprecatedResponseAndErrorResponseEqualValue", false, HttpStatus.FORBIDDEN);
    }

    @Test
    void preHandle_MethodFeature_throwISEIfDeprecatedResponseStatusAndErrorResponseStatusAreDifferent() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            // just to compile. the actual value is irrelevant, as there should be an exception because of ambiguous configuration
            final HttpStatus any = HttpStatus.valueOf(200);
            assertPreHandle("methodFeatureDeprecatedResponseAndErrorResponseDifferentValues", false, any);
        });
    }

    @Test
    void handlerAnnotation_OnType() throws NoSuchMethodException {
        final TestController controller = new TestController();
        final HandlerMethod handler = new HandlerMethod(controller, "classFeature");
        final FeaturesAreActive annotation = FeatureInterceptor.handlerAnnotation(handler, FeaturesAreActive.class);
        assertNotNull(annotation);
        assertEquals(DEFAULT_ERROR_RESPONSE_STATUS, annotation.errorResponseStatus());
        assertThat(annotation.features()).containsExactly("CLASS_FEATURE");
    }

    @Test
    void handlerAnnotation_OnMethod() throws NoSuchMethodException {
        final TestController controller = new TestController();
        final HandlerMethod handler = new HandlerMethod(controller, "methodFeatureTwo");
        final FeaturesAreActive annotation = FeatureInterceptor.handlerAnnotation(handler, FeaturesAreActive.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.FOUND, annotation.errorResponseStatus());
        assertThat(annotation.features()).containsExactly("METHOD_FEATURE", "METHOD_FEATURE_TWO");
    }

    private void enableFeature(final TestFeatures feature) {
        this.repository.setFeatureState(new FeatureState(feature, true));
        assertTrue(this.manager.isActive(feature));
    }

    private void assertPreHandle(final String methodName, final boolean expectedReturnValue, final HttpStatus expectedStatus) throws NoSuchMethodException, Exception {
        final FeatureInterceptor featureInterceptor = new FeatureInterceptor();

        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TestController controller = new TestController();
        final HandlerMethod handler = new HandlerMethod(controller, methodName);

        assertEquals(expectedReturnValue, featureInterceptor.preHandle(request, response, handler));
        assertEquals(expectedStatus.value(), response.getStatus());
    }
}


