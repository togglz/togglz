package de.chkal.togglz.servlet.ui.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.FeatureMetaData;
import de.chkal.togglz.core.context.FeatureContext;
import de.chkal.togglz.core.manager.FeatureManager;
import de.chkal.togglz.core.manager.FeatureState;
import de.chkal.togglz.servlet.ui.RequestHandlerBase;

public class OverviewPageHandler extends RequestHandlerBase {

    @Override
    public boolean handles(String path) {
        return "/index".equals(path);
    }

    @Override
    public void process(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        StringBuilder body = new StringBuilder();

        body.append("<html>");
        body.append("<table class=\"zebra-striped\">");
        body.append("<tbody>");

        body.append("<thead>");
        body.append("<tr><th>Feature</th><th>Status</th></tr>");
        body.append("</thead>");

        FeatureManager featureManager = FeatureContext.getFeatureManager();
        
        for (Feature f : featureManager.getFeatures()) {

            FeatureMetaData metaData = new FeatureMetaData(f);

            body.append("<tr>");

            body.append("<td>");
            body.append(metaData.getLabel());
            body.append("</td>");

            body.append("<td>");
            FeatureState state = featureManager.getFeatureState(f);
            if (state.isEnabled()) {
                body.append("ON");
            } else {
                body.append("OFF");
            }
            body.append("</td>");

            body.append("</tr>");
        }

        body.append("</tbody>");
        body.append("</table>");
        body.append("</html>");

        writeResponse(response, body.toString());

    }

}
