package org.togglz.console;

import java.io.IOException;

public interface RequestHandler {

    boolean handles(String path);

    void process(RequestEvent event) throws IOException;

}