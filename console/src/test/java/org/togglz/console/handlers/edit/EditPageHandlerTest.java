package org.togglz.console.handlers.edit;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.togglz.console.RequestContext;
import org.togglz.console.RequestEvent;
import org.togglz.core.Feature;
import org.togglz.testing.TestFeatureManager;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EditPageHandlerTest {

    private final EditPageHandler testee = new EditPageHandler();

    @Test
    void shouldHandleEditPath() {
        assertTrue(testee.handles("/edit"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"edit", "//edit", "/dit", "/edi"})
    void shouldNotHandleEditPaths(String path) {
        assertFalse(testee.handles(path));
    }

    @Test
    void shouldBeAdminOnly() {
        assertTrue(testee.adminOnly());
    }

    @Test
    void shouldProcessEditRequest() throws Exception {
        // given
        TestFeatureManager featureManager = new TestFeatureManager(TestFeatures.class);
        featureManager.disable(TestFeatures.FEATURE_ONE);

        EditPageHandler handler = new EditPageHandler();

        // when
        RequestEvent requestEvent = createMockPostRequestEvent(featureManager, TestFeatures.FEATURE_ONE, true);
        handler.process(requestEvent);

        // then - verify response was written without errors
        verify(requestEvent.getResponse()).addCookie(any());
        verify(requestEvent.getResponse()).sendRedirect("index");

        // and - verify feature state was updated
        assertTrue(featureManager.isActive(TestFeatures.FEATURE_ONE));
    }

    private RequestEvent createMockPostRequestEvent(TestFeatureManager featureManager, TestFeatures feature, boolean enabled) throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletContext context = mock(ServletContext.class);
        RequestContext requestContext = new RequestContext(false); // disable CSRF validation for test

        // Setup mock request
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("f")).thenReturn(feature.name());
        when(request.getParameter("enabled")).thenReturn(feature.name());
        when(request.getContextPath()).thenReturn("");
        when(request.getServletPath()).thenReturn("/togglz");
        when(request.getRequestURI()).thenReturn("/togglz/edit");

        // Setup mock response with output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ServletOutputStream servletOutputStream = new ServletOutputStream() {
            @Override
            public void write(int b) {
                outputStream.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(jakarta.servlet.WriteListener listener) {
            }
        };
        when(response.getOutputStream()).thenReturn(servletOutputStream);

        return new RequestEvent(featureManager, context, request, response, requestContext);
    }

    private enum TestFeatures implements Feature {
        FEATURE_ONE,
        FEATURE_TWO
    }
}