package de.chkal.togglz.console.handlers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.chkal.togglz.console.RequestHandlerBase;
import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.FeatureMetaData;
import de.chkal.togglz.core.context.FeatureContext;
import de.chkal.togglz.core.manager.FeatureManager;
import de.chkal.togglz.core.manager.FeatureState;

public class IndexPageHandler extends RequestHandlerBase {

    @Override
    public boolean handles(String path) {
        return path.endsWith("/index");
    }

    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException {

        StringBuilder body = new StringBuilder();

        body.append("<html>");
        body.append("<table class=\"zebra-striped feature-overview\">");

        body.append("<thead>");
        body.append(getResourceAsString("index-header.html"));
        body.append("</thead>");
        body.append("<tbody>");

        FeatureManager featureManager = FeatureContext.getFeatureManager();

        String template = getResourceAsString("index-row.html");

        for (Feature f : featureManager.getFeatures()) {

            FeatureMetaData metaData = new FeatureMetaData(f);
            FeatureState state = featureManager.getFeatureState(f);

            Map<String, String> model = new HashMap<String, String>();
            model.put("%NAME%", f.name());
            model.put("%LABEL%", metaData.getLabel());
            model.put("%IMAGE%", state.isEnabled() ? "ledgreen.png" : "ledred.png");
            model.put("%USERS%", buildUserList(state));

            body.append(evaluateTemplate(template, model));

        }

        body.append("</tbody>");
        body.append("</table>");
        body.append("</html>");

        writeResponse(response, body.toString());

    }

    private String buildUserList(FeatureState state) {
        StringBuilder users = new StringBuilder();
        for(String user : state.getUsers()) {
            if(users.length() > 0) {
                users.append(", ");
            }
            users.append(user);
        }
        return users.toString();
    }

}
