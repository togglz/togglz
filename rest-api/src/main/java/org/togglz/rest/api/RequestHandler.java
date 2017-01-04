package org.togglz.rest.api;

import java.io.IOException;
import java.io.Reader;

import org.togglz.rest.api.model.FeatureToggleRepresentation;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface RequestHandler {

    String contentType();

    FeatureToggleRepresentation deserialize(Reader reader) throws IOException;

    String serialize(Object obj) throws IOException;

}
