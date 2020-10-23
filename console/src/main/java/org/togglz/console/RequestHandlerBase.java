package org.togglz.console;

import com.floreysoft.jmte.Engine;
import org.togglz.core.Togglz;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class RequestHandlerBase implements RequestHandler {

    protected void writeResponse(RequestEvent event, String body) throws IOException {
        HttpServletResponse response = event.getResponse();

        Map<String, Object> model = new HashMap<>();
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
        outputStream.write(result.getBytes(UTF_8));
        response.flushBuffer();

    }

    protected String getResourceAsString(String name) throws IOException {
        InputStream stream = loadResource(name);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        copy(stream, bos);
        return new String(bos.toByteArray(), UTF_8);
    }

    protected InputStream loadResource(String name) {
        String templateName = RequestHandler.class.getPackage().getName().replace('.', '/') + "/" + name;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResourceAsStream(templateName);
    }

    protected void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
    }
}
