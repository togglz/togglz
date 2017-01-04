package org.togglz.rest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.togglz.rest.api.model.FeatureToggleRepresentation;

import java.io.IOException;
import java.io.Reader;

/**
 * Created by fabio on 03/01/17.
 */
public abstract class JacksonRequestHandler implements RequestHandler {

    protected final ObjectMapper mapper;

    public JacksonRequestHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public FeatureToggleRepresentation deserialize(Reader reader) throws IOException {
        return mapper.readValue(reader, FeatureToggleRepresentation.class);
    }

    @Override
    public String serialize(Object obj) throws IOException {
        return mapper.writeValueAsString(obj);
    }
}
