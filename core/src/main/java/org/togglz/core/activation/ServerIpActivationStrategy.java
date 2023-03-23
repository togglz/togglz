package org.togglz.core.activation;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger log = LoggerFactory.getLogger(ScriptEngineActivationStrategy.class);

    public static final String ID = "server-ip";

    public static final String PARAM_IPS = "ips";

    private final Set<String> ipAddresses = new HashSet<>();

    public ServerIpActivationStrategy()
    {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    Enumeration<InetAddress> addresses = interfaces.nextElement().getInetAddresses();
                    if (addresses != null) {
                        while (addresses.hasMoreElements()) {
                            String hostAddress = addresses.nextElement().getHostAddress();
                            if (hostAddress != null) {
                                ipAddresses.add(hostAddress);
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            log.error("Unable to find IP addresses: " + e.getMessage());
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
