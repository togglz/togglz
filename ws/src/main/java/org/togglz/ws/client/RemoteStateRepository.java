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
import org.togglz.ws.server.FeatureActivity;

/**
 * An implementation of {@link StateRepository} that retrieves the feature state
 * via the {@link FeatureActivity} WebService.
 * 
 * @author Mauro Talevi
 */
public class RemoteStateRepository implements StateRepository {

    private final Log log = LogFactory.getLog(RemoteStateRepository.class);

    private final String serverURL;

    public RemoteStateRepository(String serverURL) {
        this.serverURL = serverURL;
    }

    public FeatureState getFeatureState(Feature feature) {
        boolean active = false;
        String name = feature.name();
        try {
            active = create(FeatureActivity.class).isActive(name);
        } catch (MalformedURLException e) {
            throw new RemoteFeatureNotAccessible(name, e);
        }
        log.debug("Remote feature " + name + " is " + (!active ? "not" : "") + " active");
        return new FeatureState(feature, active);
    }

    public void setFeatureState(FeatureState featureState) {
        throw new UnsupportedOperationException();
    }

    private <T> T create(Class<T> serviceClass) throws MalformedURLException {
        URL wsdlURL = new URL(serverURL + "/FeatureActivity?wsdl");
        QName serviceName = new QName("http://server.ws.togglz.org/", "ContextFeatureActivityService");
        Service service = Service.create(wsdlURL, serviceName);
        return service.getPort(serviceClass);
    }

    @SuppressWarnings("serial")
    public static class RemoteFeatureNotAccessible extends RuntimeException {

        public RemoteFeatureNotAccessible(String name, MalformedURLException e) {
            super(name, e);
        }

    }
}
