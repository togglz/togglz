package org.togglz.core.manager;

import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.util.NamedFeature;

import static org.junit.jupiter.api.Assertions.*;

class CompositeFeatureProviderTest {

	@Test
	void empty() {
		CompositeFeatureProvider provider = new CompositeFeatureProvider();
		assertEquals(0, provider.getFeatures().size());
		assertNull(provider.getMetaData(new NamedFeature("FOO")));
	}

	@Test
	void oneProvider() {
		@SuppressWarnings("unchecked")
		CompositeFeatureProvider provider = new CompositeFeatureProvider(new EnumBasedFeatureProvider(TestFeatures.class));
		assertEquals(2, provider.getFeatures().size());
		assertNotNull(provider.getMetaData(new NamedFeature("FOO")));
	}

	enum TestFeatures implements Feature {
		FOO, BAR;
	}
}
