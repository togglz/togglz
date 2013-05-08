package org.togglz.core.activation;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;

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

    private String getMachineIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    private enum MyFeature implements Feature {
        FEATURE;
    }

}
