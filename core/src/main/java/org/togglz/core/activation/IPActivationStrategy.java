package org.togglz.core.activation;

import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Activation strategy that allows to activate features only for certain ip's.
 * 
 * @author Eli Abramovitch
 */
public class IPActivationStrategy implements ActivationStrategy {

    public static final String ID = "ip";

    public static final String PARAM_IPS = "ips";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "IP";
    }

    @Override
    public boolean isActive(FeatureState featureState, FeatureUser user) {
        String ipsAsString = featureState.getParameter(PARAM_IPS);

              if (Strings.isNotBlank(ipsAsString)) {

                  List<String> ips = Strings.splitAndTrim(ipsAsString, ",");
                  String currentMachineIp = null;
                  try {
                      currentMachineIp = InetAddress.getLocalHost().getHostAddress();
                  } catch (UnknownHostException e) {
                      e.printStackTrace();
                  }
                  if (currentMachineIp != null) {
                      for (String ip : ips) {
                          if (currentMachineIp.equals(ip)) {
                              return true;
                          }
                      }
                  }
              }
              return false;
    }


    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
                ParameterBuilder.create(PARAM_IPS).label("IPs").largeText()
                    .description("A list of ips for which the feature is active.")
        };
    }

}
