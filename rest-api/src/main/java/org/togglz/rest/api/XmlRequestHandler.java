package org.togglz.rest.api;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.inject.Inject;

public class XmlRequestHandler extends JacksonRequestHandler {

    static final String APPLICATION_XML = "application/xml";

    public XmlRequestHandler() {
        this(new XmlMapper());
    }

    @Inject
    public XmlRequestHandler(XmlMapper mapper) {
        super(mapper);
    }

    @Override
    public String contentType() {
        return APPLICATION_XML;
    }

}
