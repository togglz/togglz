package org.togglz.core.manager;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.util.NamedFeature;

public class CompositeFeatureProviderTest {

	@Test
	public void empty() {
		CompositeFeatureProvider provider = new CompositeFeatureProvider();
		assertThat(provider.getFeatures()).isEmpty();
		assertThat(provider.getMetaData(new NamedFeature("FOO"))).isNull();
	}

	@Test
	public void oneProvider() {
		@SuppressWarnings("unchecked")
		CompositeFeatureProvider provider = new CompositeFeatureProvider(new EnumBasedFeatureProvider(TestFeatures.class));
		assertThat(provider.getFeatures()).hasSize(2);
		assertThat(provider.getMetaData(new NamedFeature("FOO"))).isNotNull();
	}

	enum TestFeatures implements Feature {
		FOO, BAR;
	}
}
