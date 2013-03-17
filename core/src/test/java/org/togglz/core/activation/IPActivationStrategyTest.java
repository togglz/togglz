package org.togglz.core.activation;

import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class IPActivationStrategyTest {

    private final IPActivationStrategy strategy = new IPActivationStrategy();

    @Test
    public void shouldReturnFalseForEmptyIPlist() {
        FeatureUser user = new SimpleFeatureUser("ea", false);
        FeatureState state = new FeatureState(MyFeature.FEATURE).enable().setStrategyId(IPActivationStrategy.ID);
        boolean active = strategy.isActive(state, user);
        assertEquals(false, active);
    }

    @Test
    public void shouldReturnTrueForFeatureOnCorrectMachine() {
        FeatureUser user = new SimpleFeatureUser("ea", false);
        FeatureState state = new FeatureState(MyFeature.FEATURE).enable().setStrategyId(IPActivationStrategy.ID);
        state.setParameter(IPActivationStrategy.PARAM_IPS, getMachineIP());
        boolean active = strategy.isActive(state, user);
        assertEquals(true, active);
    }

    @Test
    public void shouldReturnFalseForFeatureOnOtherMachine() {
        FeatureUser user = new SimpleFeatureUser("ea", false);
        FeatureState state = new FeatureState(MyFeature.FEATURE).enable().setStrategyId(IPActivationStrategy.ID);
        state.setParameter(IPActivationStrategy.PARAM_IPS, "1.1.1.1");
        boolean active = strategy.isActive(state, user);
        assertEquals(false, active);
    }

    @Test
    public void multipleMachineIps() {
        FeatureUser user = new SimpleFeatureUser("ea", false);
        FeatureState state = new FeatureState(MyFeature.FEATURE).enable().setStrategyId(IPActivationStrategy.ID);
        state.setParameter(IPActivationStrategy.PARAM_IPS, "1.1.1.1,2.2.2.2," + getMachineIP());
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
