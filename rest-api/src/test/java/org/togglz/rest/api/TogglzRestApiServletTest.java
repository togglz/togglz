package org.togglz.rest.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONValue;

import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;
import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;

public class TogglzRestApiServletTest {

    private ServletTester servletTester;

    @Before
    public void setup() throws Exception {
        servletTester = new ServletTester();
        servletTester.addServlet(TogglzRestApiServlet.class, "/api/v1/featuretoggles/*");
        servletTester.start();
    }

    @Test
    public void postShouldReturnNotAllowed() throws Exception {
        HttpTester request = getFeatureRequest("", "POST");
        HttpTester response = response(request);
        assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    public void deleteShouldReturnNotAllowed() throws Exception {
        HttpTester request = getFeatureRequest("", "DELETE");
        HttpTester response = response(request);
        assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    public void optionsShouldReturnNotAllowed() throws Exception {
        HttpTester request = getFeatureRequest("", "OPTIONS");
        HttpTester response = response(request);
        assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, response.getStatus());
    }
    
    @Test
    public void traceShouldReturnNotAllowed() throws Exception {
        HttpTester request = getFeatureRequest("", "TRACE");
        HttpTester response = response(request);
        assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, response.getStatus());
    }
    
    @Test
    public void testGetAllFeatures() throws Exception {
        HttpTester request = getFeatureRequest("", "GET");
        HttpTester response = response(request);
        assertEquals(200, response.getStatus());
        JSONArray value = (JSONArray) JSONValue.parse(response.getContent());
        assertNotNull(value);
        assertEquals(3, value.size());
        assertContentType(response);
    }

    @Test
    public void testGetAllFeatures2() throws Exception {
        HttpTester request = getFeatureRequest("", "GET");
        request.setURI("/api/v1/featuretoggles");
        HttpTester response = response(request);
        assertEquals(200, response.getStatus());
        assertEquals("[{\"enabled\":false,\"name\":\"F1\"},{\"enabled\":false,\"name\":\"F2\"},{\"enabled\":true,\"name\":\"F3\"}]",response.getContent());
        assertContentType(response);
    }

    private void assertContentType(HttpTester response) {
        assertEquals("application/json",response.getHeader("Content-Type"));
    }

    @Test
    public void testGetOneFeature() throws Exception {
        HttpTester request = getFeatureRequest("F1", "GET");
        HttpTester response = response(request);
        assertEquals(200, response.getStatus());
        assertEquals("{\"enabled\":false,\"name\":\"F1\"}",response.getContent());
        assertContentType(response);
    }

    @Test
    public void testGetNonExistentFeature() throws Exception {
        HttpTester request = getFeatureRequest("F10", "GET");
        HttpTester response = response(request);
        assertEquals(404, response.getStatus());
    }

    private HttpTester getFeatureRequest(String featureName, String method) {
        HttpTester request = new HttpTester();
        request.setMethod(method);
        request.setURI("/api/v1/featuretoggles/" + featureName);
        request.setVersion("HTTP/1.0");
        request.addHeader("Content-Type", "application/json");
        return request;
    }
    
    @Test
    public void testPutFeature() throws Exception {
        HttpTester request = getFeatureRequest("F1", "PUT");
        request.setContent("{\"enabled\":true,\"name\":\"F1\"}");
        assertEquals(200, response(request).getStatus());
        assertEquals("{\"enabled\":true,\"name\":\"F1\"}",response(getFeatureRequest("F1", "GET")).getContent());
    }

    @Test
    public void putFeatureInvalidHeaders() throws Exception {
        HttpTester request = getFeatureRequest("F1", "PUT");
        request.setHeader("Content-Type", "text/xml");
        assertEquals(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, response(request).getStatus());
    }
    
    private HttpTester response(HttpTester request) throws IOException, Exception {
        HttpTester response = new HttpTester();
        response.parse(servletTester.getResponses(request.generate()));
        return response;
    }
    
    public enum TestFeatures implements Feature {
        F1,
        F2,
        @EnabledByDefault
        F3
        
    }

}