package org.togglz.core.user;

public class NamedUserProvider implements UserProvider {
	private final String username;
	private final boolean featureAdmin;
	
	public NamedUserProvider(String username, boolean featureAdmin) {
		this.username = username;
		this.featureAdmin = featureAdmin;
	}

	@Override
	public FeatureUser getCurrentUser() {
		return new SimpleFeatureUser(username, featureAdmin);
	}

}
