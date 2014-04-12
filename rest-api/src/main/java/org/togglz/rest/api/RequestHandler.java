package org.togglz.rest.api;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.togglz.rest.api.model.FeatureToggle;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface RequestHandler {

    String serialize(FeatureToggle feature) throws JsonProcessingException;

    String contentType();

    String serialize(List<FeatureToggle> features) throws JsonProcessingException;

    FeatureToggle desserialize(Reader reader) throws JsonParseException, JsonMappingException, IOException;

}
