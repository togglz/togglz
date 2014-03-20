package org.togglz.rest.api;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

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
    public void testGetAllFeatures() throws Exception {
        HttpTester request = new HttpTester();
        request.setMethod("GET");
        request.setURI("/api/v1/featuretoggles/");
        request.setVersion("HTTP/1.0");
        request.addHeader("Content-Type", "application/json");
        
        HttpTester response = response(request);
        assertEquals(200, response.getStatus());
        assertEquals("[{\"enabled\":false,\"name\":\"F1\"},{\"enabled\":false,\"name\":\"F2\"},{\"enabled\":true,\"name\":\"F3\"}]",response.getContent());
        assertContentType(response);
    }

    @Test
    public void testGetAllFeatures2() throws Exception {
        HttpTester request = new HttpTester();
        request.setMethod("GET");
        request.setURI("/api/v1/featuretoggles");
        request.setVersion("HTTP/1.0");
        request.addHeader("Content-Type", "application/json");
        
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
        HttpTester request = getFeatureRequest("F1");
        HttpTester response = response(request);
        assertEquals(200, response.getStatus());
        assertEquals("{\"enabled\":false,\"name\":\"F1\"}",response.getContent());
        assertContentType(response);
    }

    @Test
    public void testGetNonExistentFeature() throws Exception {
        HttpTester request = getFeatureRequest("F10");
        HttpTester response = response(request);
        assertEquals(404, response.getStatus());
    }

    private HttpTester getFeatureRequest(String featureName) {
        HttpTester request = new HttpTester();
        request.setMethod("GET");
        request.setURI("/api/v1/featuretoggles/" + featureName);
        request.setVersion("HTTP/1.0");
        request.addHeader("Content-Type", "application/json");
        return request;
    }

    
    @Test
    public void testPutFeature() throws Exception {
        HttpTester request = new HttpTester();
        request.setMethod("PUT");
        request.setURI("/api/v1/featuretoggles/F1");
        request.setVersion("HTTP/1.0");
        request.addHeader("Content-Type", "application/json");
        request.setContent("{\"enabled\":true,\"name\":\"F1\"}");
        
        HttpTester response = response(request);
        assertEquals(200, response.getStatus());
        
        HttpTester response2 = response(getFeatureRequest("F1"));
        assertEquals("{\"enabled\":true,\"name\":\"F1\"}",response2.getContent());
        
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