package org.togglz.rest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.activation.UsernameActivationStrategy;
import org.togglz.core.annotation.ActivationParameter;
import org.togglz.core.annotation.DefaultActivationStrategy;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.rest.api.model.FeatureToggleRepresentation;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TogglzRestApiServletTest {

    private static final String F10 = "F10";
    private static final String F1 = "F1";
    private static final String F3 = "F3";
    private static final String BASE_URI = "/api/v1/featuretoggles";
    private static final String GET = "GET";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    private ObjectMapper mapper = new ObjectMapper();

    private ServletTester servletTester;

    @Before
    public void setup() throws Exception {
        servletTester = new ServletTester();
        servletTester.addServlet(TogglzRestApiServlet.class, BASE_URI + "/*");
        servletTester.start();
    }

    @After
    public void after() throws Exception {
        servletTester.stop();
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
    public void shouldReturnAllFeatures() throws Exception {
        HttpTester request = getFeatureRequest("", GET);
        assertAllFeatures(response(request));
    }

    private void assertAllFeatures(HttpTester response) {
        Object parse = JSONValue.parse(response.getContent());
        System.out.println(parse);

        JSONArray value = (JSONArray) parse;
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertNotNull(value);
        assertEquals(3, value.size());
        assertContentType(response);
    }

    @Test
    public void getWithoutSlashShouldReturnAllFeatures() throws Exception {
        HttpTester request = getFeatureRequest("", GET);
        request.setURI(BASE_URI);
        assertAllFeatures(response(request));
    }

    private void assertContentType(HttpTester response) {
        assertTrue(response.getHeader(CONTENT_TYPE).startsWith(APPLICATION_JSON));
    }

    @Test
    public void testGetOneFeature() throws Exception {
        HttpTester request = getFeatureRequest(F1, GET);
        HttpTester response = response(request);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());

        assertEquals(false, (Boolean) featureAsJson(response).get("enabled"));
        assertEquals(F1, (String) featureAsJson(response).get("name"));

        assertContentType(response);
    }

    private JSONObject featureAsJson(HttpTester response) {
        return (JSONObject) JSONValue.parse(response.getContent());
    }

    @Test
    public void getNonExistentFeatureShouldReturnNotFound() throws Exception {
        HttpTester request = getFeatureRequest(F10, GET);
        HttpTester response = response(request);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }

    private HttpTester getFeatureRequest(String featureName, String method) {
        HttpTester request = new HttpTester();
        request.setMethod(method);
        request.setURI(BASE_URI + "/" + featureName);
        request.setVersion("HTTP/1.0");
        request.addHeader(CONTENT_TYPE, APPLICATION_JSON);
        request.addHeader("Accept", APPLICATION_JSON);
        return request;
    }

    @Test
    public void testPutFeature() throws Exception {
        final String f1EnabledAsJson = "{\"name\":\"F1\",\"enabled\":true}";
        HttpTester request = getFeatureRequest(F1, "PUT");
        request.setContent(f1EnabledAsJson);
        assertEquals(HttpServletResponse.SC_OK, response(request).getStatus());

        assertEquals(f1EnabledAsJson, response(getFeatureRequest(F1, GET)).getContent());
    }

    @Test
    public void putFeatureChangingStrategyParam() throws Exception {
        final String f1EnabledAsJson = "{\"enabled\":true,\"name\":\"F3\",\"strategy\":{\"id\":\"username\",\"parameters\":[{\"name\":\"users\",\"value\":\"person1\"}]}}";
        HttpTester request = getFeatureRequest(F3, "PUT");
        request.setContent(f1EnabledAsJson);
        assertEquals(HttpServletResponse.SC_OK, response(request).getStatus());

        FeatureToggleRepresentation response = mapper.readValue(response(getFeatureRequest(F3, GET)).getContent(),
            FeatureToggleRepresentation.class);

        assertEquals("username", response.getStrategyId());
        assertEquals("person1", response.getParameter("users"));
    }

    @Test
    public void unregisteredStrategyShouldReturnBadRequest() throws Exception {
        final String f1EnabledAsJson = "{\"enabled\":true,\"name\":\"F1\",\"strategy\":{\"id\":\"unregistered\",\"parameters\":[{\"name\":\"users\",\"value\":\"person1\"}]}}";
        HttpTester request = getFeatureRequest(F1, "PUT");
        request.setContent(f1EnabledAsJson);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response(request).getStatus());
    }

    @Test
    public void putFeatureInvalidContentTYpe() throws Exception {
        HttpTester request = getFeatureRequest(F1, "PUT");
        request.setHeader(CONTENT_TYPE, "text/xml");
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
        @DefaultActivationStrategy(
            id = UsernameActivationStrategy.ID,
            parameters = {
                    @ActivationParameter(name = UsernameActivationStrategy.PARAM_USERS, value = "person1,ck,person2")
            }
        )
        F3
    }

}
