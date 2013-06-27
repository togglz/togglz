package org.togglz.core.activation;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;

/**
 * Activation strategy that allows to activate features only for certain server IPs.
 * 
 * @author Eli Abramovitch
 * @author Christian Kaltepoth
 */
public class ServerIpActivationStrategy implements ActivationStrategy {

    public static final String ID = "server-ip";

    public static final String PARAM_IPS = "ips";

    private final Set<String> ipAddresses = new HashSet<String>();

    public ServerIpActivationStrategy()
    {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                Enumeration<InetAddress> interfacesAddresses = interfaces.nextElement().getInetAddresses();
                while (interfacesAddresses.hasMoreElements()) {
                    ipAddresses.add(interfacesAddresses.nextElement().getHostAddress());
                }
            }
        } catch (SocketException e) {
            throw new IllegalStateException("Unable to find IP addresses", e);
        }
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "IP address (server)";
    }

    @Override
    public boolean isActive(FeatureState featureState, FeatureUser user) {

        String allowedIpsParam = featureState.getParameter(PARAM_IPS);

        if (Strings.isNotBlank(allowedIpsParam)) {

            List<String> allowedIps = Strings.splitAndTrim(allowedIpsParam, "[\\s,]+");

            for (String allowedIp : allowedIps) {
                if (ipAddresses.contains(allowedIp)) {
                    return true;
                }
            }

        }

        return false;

    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
                ParameterBuilder.create(PARAM_IPS).label("Server IPs")
                    .description("A comma-separated list of server IPs for which the feature should be active.")
        };
    }

}
