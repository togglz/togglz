package org.togglz.core.bootstrap;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.UserProvider;

public class ConstructorBasedBootstrapperTest {

    @Test
    public void canBootstrapViaConstructor()  {
    	FeatureProvider featureProvider = mock(FeatureProvider.class);
		StateRepository stateRepository = mock(StateRepository.class);
		UserProvider userProvider = mock(UserProvider.class);
		TogglzBootstrap bootstraper = new ConstructorBasedBootstrapper(featureProvider, stateRepository, userProvider);
		assertThat(bootstraper.createFeatureManager(), notNullValue());
    }

}
