package org.togglz.core.bootstrap;

import org.togglz.core.bootstrap.TogglzBootstrap;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.UserProvider;

public class ConstructorBasedBootstrapper implements TogglzBootstrap {

	private FeatureProvider featureProvider;
	private StateRepository stateRepository;
	private UserProvider userProvider;

	public ConstructorBasedBootstrapper(FeatureProvider featureProvider,
			StateRepository stateRepository, UserProvider userProvider) {
		this.featureProvider = featureProvider;
		this.stateRepository = stateRepository;
		this.userProvider = userProvider;
	}

	@Override
	public FeatureManager createFeatureManager() {
		return new FeatureManagerBuilder().featureProvider(featureProvider)
				.stateRepository(stateRepository).userProvider(userProvider)
				.build();
	}

}
