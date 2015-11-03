package org.togglz.rest.api;

import java.io.IOException;
import java.io.Reader;

import org.togglz.rest.api.model.FeatureToggleRepresentation;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface RequestHandler {

    String contentType();

    FeatureToggleRepresentation desserialize(Reader reader) throws JsonParseException, JsonMappingException, IOException;

    String serialize(Object obj) throws IOException;

}
