package org.togglz.hazelcast;

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.util.NamedFeature;

public class HazelcastStateRepositoryTest {

	private StateRepository stateRepository = HazelcastStateRepository.newBuilder().mapName("togglzMap").build();

	@Test
	public void testSetFeatureState() {
		Feature feature = new NamedFeature("SAMPLE_FEATURE");
		FeatureState featureState = new FeatureState(feature, true);
		stateRepository.setFeatureState(featureState);
		
		FeatureState storedFeatureState = stateRepository.getFeatureState(feature);
		
		assertTrue(EqualsBuilder.reflectionEquals(featureState, storedFeatureState, true));
		
	}

}
