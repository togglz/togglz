package org.togglz.core.activation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;

public class IPActivationStrategyTest {

    private final ServerIpActivationStrategy strategy = new ServerIpActivationStrategy();

    @Test
    public void shouldReturnFalseForEmptyIPlist() {
        FeatureUser user = new SimpleFeatureUser("ea", false);
        FeatureState state = new FeatureState(MyFeature.FEATURE).enable().setStrategyId(ServerIpActivationStrategy.ID);
        boolean active = strategy.isActive(state, user);
        assertEquals(false, active);
    }

    @Test
    public void shouldReturnTrueForFeatureOnCorrectMachine() {
        FeatureUser user = new SimpleFeatureUser("ea", false);
        FeatureState state = new FeatureState(MyFeature.FEATURE).enable().setStrategyId(ServerIpActivationStrategy.ID);
        state.setParameter(ServerIpActivationStrategy.PARAM_IPS, getMachineIP());
        boolean active = strategy.isActive(state, user);
        assertEquals(true, active);
    }

    @Test
    public void shouldReturnFalseForFeatureOnOtherMachine() {
        FeatureUser user = new SimpleFeatureUser("ea", false);
        FeatureState state = new FeatureState(MyFeature.FEATURE).enable().setStrategyId(ServerIpActivationStrategy.ID);
        state.setParameter(ServerIpActivationStrategy.PARAM_IPS, "1.1.1.1");
        boolean active = strategy.isActive(state, user);
        assertEquals(false, active);
    }

    @Test
    public void multipleMachineIps() {
        FeatureUser user = new SimpleFeatureUser("ea", false);
        FeatureState state = new FeatureState(MyFeature.FEATURE).enable().setStrategyId(ServerIpActivationStrategy.ID);
        state.setParameter(ServerIpActivationStrategy.PARAM_IPS, "1.1.1.1,2.2.2.2," + getMachineIP());
        boolean active = strategy.isActive(state, user);
        assertEquals(true, active);
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
                    if (addresses != null) {
                        while (addresses.hasMoreElements()) {
                            String hostAddress = addresses.nextElement().getHostAddress();
                            if (hostAddress != null) {
                                return hostAddress.trim();
                            }
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
