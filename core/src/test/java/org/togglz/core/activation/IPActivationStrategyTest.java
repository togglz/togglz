package org.togglz.core.activation;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;

import static org.junit.jupiter.api.Assertions.*;

class IPActivationStrategyTest {

    private final ServerIpActivationStrategy strategy = new ServerIpActivationStrategy();

    @Test
    void shouldReturnFalseForEmptyIPlist() {
        FeatureUser user = new SimpleFeatureUser("ea", false);
        FeatureState state = new FeatureState(MyFeature.FEATURE).enable().setStrategyId(ServerIpActivationStrategy.ID);
        boolean active = strategy.isActive(state, user);
        assertFalse(active);
    }

    @Test
    void shouldReturnTrueForFeatureOnCorrectMachine() {
        FeatureUser user = new SimpleFeatureUser("ea", false);
        FeatureState state = new FeatureState(MyFeature.FEATURE).enable().setStrategyId(ServerIpActivationStrategy.ID);
        state.setParameter(ServerIpActivationStrategy.PARAM_IPS, getMachineIP());
        boolean active = strategy.isActive(state, user);
        assertTrue(active);
    }

    @Test
    void shouldReturnFalseForFeatureOnOtherMachine() {
        FeatureUser user = new SimpleFeatureUser("ea", false);
        FeatureState state = new FeatureState(MyFeature.FEATURE).enable().setStrategyId(ServerIpActivationStrategy.ID);
        state.setParameter(ServerIpActivationStrategy.PARAM_IPS, "1.1.1.1");
        boolean active = strategy.isActive(state, user);
        assertFalse(active);
    }

    @Test
    void multipleMachineIps() {
        FeatureUser user = new SimpleFeatureUser("ea", false);
        FeatureState state = new FeatureState(MyFeature.FEATURE).enable().setStrategyId(ServerIpActivationStrategy.ID);
        state.setParameter(ServerIpActivationStrategy.PARAM_IPS, "1.1.1.1,2.2.2.2," + getMachineIP());
        boolean active = strategy.isActive(state, user);
        assertTrue(active);
    }

    /**
     * Returns the first IP of the current machine
     */
    private String getMachineIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    Enumeration<InetAddress> addresses = interfaces.nextElement().getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        String hostAddress = addresses.nextElement().getHostAddress();
                        if (hostAddress != null) {
                            return hostAddress.trim();
                        }
                    }
                }
            }
            return null;
        } catch (SocketException e) {
            return "<no-ip>";
        }
    }

    private enum MyFeature implements Feature {
        FEATURE
    }
}
