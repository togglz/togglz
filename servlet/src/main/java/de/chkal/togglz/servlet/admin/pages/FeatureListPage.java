package de.chkal.togglz.servlet.admin.pages;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.manager.FeatureManager;
import de.chkal.togglz.servlet.admin.AdminPage;

public class FeatureListPage extends AdminPage {

    private final FeatureManager featureManager;

    public FeatureListPage(FeatureManager featureManager) {
        this.featureManager = featureManager;
    }

    @Override
    public boolean handles(String path) {
        return "/index".equals(path);
    }

    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException {

        StringBuilder body = new StringBuilder();

        body.append("<html>");
        body.append("<table class=\"zebra-striped\">");
        body.append("<tbody>");

        body.append("<thead>");
        body.append("<tr><th>Feature</th><th>Status</th></tr>");
        body.append("</thead>");

        for (Feature f : featureManager.getFeatures()) {

            body.append("<tr>");

            body.append("<td>");
            body.append(f.name());
            body.append("</td>");

            body.append("<td>");
            if (f.isEnabled()) {
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
