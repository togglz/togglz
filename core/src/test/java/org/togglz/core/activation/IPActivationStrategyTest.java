package org.togglz.core.activation;

<<<<<<< HEAD
=======
import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;

>>>>>>> 5712285f6ef259851efaa373c5654eb30e0a3e67
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;

<<<<<<< HEAD
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;

public class IPActivationStrategyTest {

    private final IPActivationStrategy strategy = new IPActivationStrategy();
=======
public class IPActivationStrategyTest {

    private final ServerIpActivationStrategy strategy = new ServerIpActivationStrategy();
>>>>>>> 5712285f6ef259851efaa373c5654eb30e0a3e67

    @Test
    public void shouldReturnFalseForEmptyIPlist() {
        FeatureUser user = new SimpleFeatureUser("ea", false);
<<<<<<< HEAD
        FeatureState state = new FeatureState(MyFeature.FEATURE).enable().setStrategyId(IPActivationStrategy.ID);
=======
        FeatureState state = new FeatureState(MyFeature.FEATURE).enable().setStrategyId(ServerIpActivationStrategy.ID);
>>>>>>> 5712285f6ef259851efaa373c5654eb30e0a3e67
        boolean active = strategy.isActive(state, user);
        assertEquals(false, active);
    }

    @Test
    public void shouldReturnTrueForFeatureOnCorrectMachine() {
        FeatureUser user = new SimpleFeatureUser("ea", false);
<<<<<<< HEAD
        FeatureState state = new FeatureState(MyFeature.FEATURE).enable().setStrategyId(IPActivationStrategy.ID);
        state.setParameter(IPActivationStrategy.PARAM_IPS, getMachineIP());
=======
        FeatureState state = new FeatureState(MyFeature.FEATURE).enable().setStrategyId(ServerIpActivationStrategy.ID);
        state.setParameter(ServerIpActivationStrategy.PARAM_IPS, getMachineIP());
>>>>>>> 5712285f6ef259851efaa373c5654eb30e0a3e67
        boolean active = strategy.isActive(state, user);
        assertEquals(true, active);
    }

    @Test
    public void shouldReturnFalseForFeatureOnOtherMachine() {
        FeatureUser user = new SimpleFeatureUser("ea", false);
<<<<<<< HEAD
        FeatureState state = new FeatureState(MyFeature.FEATURE).enable().setStrategyId(IPActivationStrategy.ID);
        state.setParameter(IPActivationStrategy.PARAM_IPS, "1.1.1.1");
=======
        FeatureState state = new FeatureState(MyFeature.FEATURE).enable().setStrategyId(ServerIpActivationStrategy.ID);
        state.setParameter(ServerIpActivationStrategy.PARAM_IPS, "1.1.1.1");
>>>>>>> 5712285f6ef259851efaa373c5654eb30e0a3e67
        boolean active = strategy.isActive(state, user);
        assertEquals(false, active);
    }

    @Test
    public void multipleMachineIps() {
        FeatureUser user = new SimpleFeatureUser("ea", false);
<<<<<<< HEAD
        FeatureState state = new FeatureState(MyFeature.FEATURE).enable().setStrategyId(IPActivationStrategy.ID);
        state.setParameter(IPActivationStrategy.PARAM_IPS, "1.1.1.1,2.2.2.2," + getMachineIP());
=======
        FeatureState state = new FeatureState(MyFeature.FEATURE).enable().setStrategyId(ServerIpActivationStrategy.ID);
        state.setParameter(ServerIpActivationStrategy.PARAM_IPS, "1.1.1.1,2.2.2.2," + getMachineIP());
>>>>>>> 5712285f6ef259851efaa373c5654eb30e0a3e67
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

        @Override
        public boolean isActive() {
            return FeatureContext.getFeatureManager().isActive(this);
        }

    }

}
