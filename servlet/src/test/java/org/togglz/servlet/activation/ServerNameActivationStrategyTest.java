package org.togglz.servlet.activation;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;

public class ServerNameActivationStrategyTest {

    private static final String MATCHING_VHOST_NAME = "enabled.example.com";
    private static final String DIFFERENT_VHOST_NAME = "disabled.example.com";

    private ServerNameActivationStrategy strategySpy;
    private FeatureUser user;
    private FeatureState state;

    @Before
    public void init() {
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        given(requestMock.getServerName()).willReturn(MATCHING_VHOST_NAME);

        strategySpy = spy(new ServerNameActivationStrategy());
        willReturn(requestMock).given(strategySpy).getServletRequest();

        user = new SimpleFeatureUser("ea", false);
        state = new FeatureState(TestFeature.TEST_FEATURE).enable().setStrategyId(ServerNameActivationStrategy.ID);
    }

    @Test
    public void shouldReturnFalseForEmptyDomainList() {
        // when
        boolean active = strategySpy.isActive(state, user);
        // then
        assertThat(active).isFalse();
    }

    @Test
    public void shouldReturnTrueForFeatureOnMatchingDomainName() {
        // given
        state.setParameter(ServerNameActivationStrategy.PARAM_SERVER_NAMES, MATCHING_VHOST_NAME);
        // when
        boolean active = strategySpy.isActive(state, user);
        // then
        assertThat(active).isTrue();
    }

    @Test
    public void shouldReturnFalseForFeatureOnDifferentDomainName() {
        // given
        state.setParameter(ServerNameActivationStrategy.PARAM_SERVER_NAMES, DIFFERENT_VHOST_NAME);
        // when
        boolean active = strategySpy.isActive(state, user);
        // then
        assertThat(active).isFalse();
    }

    @Test
    public void shouldReturnTrueForFeatureOnMatchingOneDomainNameFromDomainList() {
        // given
        state.setParameter(ServerNameActivationStrategy.PARAM_SERVER_NAMES,
            format("%s,%s", MATCHING_VHOST_NAME, DIFFERENT_VHOST_NAME));
        // when
        boolean active = strategySpy.isActive(state, user);
        // then
        assertThat(active).isTrue();
    }

    private enum TestFeature implements Feature {
        TEST_FEATURE
    }
}
