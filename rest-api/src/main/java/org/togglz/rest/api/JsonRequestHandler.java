package org.togglz.rest.api;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.togglz.rest.api.model.FeatureToggle;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonRequestHandler implements RequestHandler {

    private static final String APPLICATION_JSON = "application/json";
    
    protected ObjectMapper mapper = new ObjectMapper();
   
    @Override
    public String serialize(FeatureToggle feature) throws JsonProcessingException {
        return mapper.writeValueAsString(feature);
    }

    @Override
    public String contentType() {
        return APPLICATION_JSON;
    }

    @Override
    public String serialize(List<FeatureToggle> features) throws JsonProcessingException {
        return mapper.writeValueAsString(features);
    }

    @Override
    public FeatureToggle desserialize(Reader reader) throws JsonParseException, JsonMappingException, IOException {
        return mapper.readValue(reader, FeatureToggle.class);
    }

}
