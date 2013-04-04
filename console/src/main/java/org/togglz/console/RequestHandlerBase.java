package org.togglz.console;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.togglz.core.Togglz;

import com.floreysoft.jmte.Engine;

public abstract class RequestHandlerBase implements RequestHandler {

    private final Charset UTF8 = Charset.forName("UTF8");

    protected void writeResponse(RequestEvent event, String body) throws IOException {

        HttpServletResponse response = event.getResponse();

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("content", body);
        model.put("serverInfo", event.getContext().getServerInfo());
        model.put("togglzTitle", Togglz.getNameWithVersion());
        if (event.getContext().getServletContextName() != null) {
            model.put("displayName", event.getContext().getServletContextName());
        }

        String template = getResourceAsString("template.html");
        String result = new Engine().transform(template, model);

        response.setContentType("text/html");
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(result.getBytes(UTF8));
        response.flushBuffer();

    }

    protected String getResourceAsString(String name) throws IOException {
        InputStream stream = loadResource(name);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        copy(stream, bos);
        return new String(bos.toByteArray(), UTF8);
    }

    protected InputStream loadResource(String name) {
        String templateName = RequestHandler.class.getPackage().getName().replace('.', '/') + "/" + name;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResourceAsStream(templateName);
    }

    protected void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
    }



}
