package org.togglz.rest.api;

import java.io.IOException;
import java.io.Reader;

import org.togglz.rest.api.model.FeatureToggleRepresentation;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class XmlRequestHandler implements RequestHandler {

    static final String APPLICATION_XML = "application/xml";
    
    protected ObjectMapper mapper = new XmlMapper();
    
    @Override
    public String contentType() {
        return APPLICATION_XML;
    }

    @Override
    public FeatureToggleRepresentation desserialize(Reader reader) throws JsonParseException, JsonMappingException, IOException {
        return mapper.readValue(reader, FeatureToggleRepresentation.class);
    }

    @Override
    public String serialize(Object obj) throws IOException {
        return mapper.writeValueAsString(obj);
    }

}
