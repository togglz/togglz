package org.togglz.ws.client;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.togglz.core.Feature;
import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.ws.server.TogglzWebService;

/**
 * An implementation of {@link StateRepository} that retrieves the feature state
 * via the {@link TogglzWebService}.
 * 
 * @author Mauro Talevi
 */
public class WebServiceStateRepository implements StateRepository {

    private final Log log = LogFactory.getLog(WebServiceStateRepository.class);

    private final String serverURL;

    private TogglzWebService webService;

    public WebServiceStateRepository(String serverURL) {
        this.serverURL = serverURL;
    }

    public FeatureState getFeatureState(Feature feature) {
        boolean active = false;
        String name = feature.name();
        active = webService().isFeatureActive(name);
        log.debug("Remote feature " + name + " is " + (!active ? "not" : "") + " active");
        return new FeatureState(feature, active);
    }

    public void setFeatureState(FeatureState featureState) {
        throw new UnsupportedOperationException();
    }

    private TogglzWebService webService() {
        if (webService == null) {
            webService = create(TogglzWebService.class);
        }
        return webService;
    }

    private <T> T create(Class<T> serviceClass) {
        try {
            URL wsdlURL = new URL(serverURL + "/TogglzWebService?wsdl");
            QName serviceName = new QName("http://server.ws.togglz.org/", "TogglzWebService");
            Service service = Service.create(wsdlURL, serviceName);
            return service.getPort(serviceClass);
        } catch (MalformedURLException e) {
            throw new WebServiceNotAccessible(e);
        }
    }

    @SuppressWarnings("serial")
    public static class WebServiceNotAccessible extends RuntimeException {

        public WebServiceNotAccessible(MalformedURLException e) {
            super(e);
        }

    }
}
