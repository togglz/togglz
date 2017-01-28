package org.togglz.rest.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;

public class JsonRequestHandler extends JacksonRequestHandler {

    static final String APPLICATION_JSON = "application/json";

    public JsonRequestHandler() {
        this(new ObjectMapper());
    }

    @Inject
    public JsonRequestHandler(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public String contentType() {
        return APPLICATION_JSON;
    }

}
