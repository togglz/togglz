package org.togglz.hazelcast;

import static org.junit.Assert.assertFalse;
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
	public void testSetFeatureStateNotExisitingInMap() {
		final Feature feature = new NamedFeature("SAMPLE_FEATURE");
		final FeatureState featureState = new FeatureState(feature, true);
		stateRepository.setFeatureState(featureState);
		
		final FeatureState storedFeatureState = stateRepository.getFeatureState(feature);
		
		assertTrue(EqualsBuilder.reflectionEquals(featureState, storedFeatureState, true));
	}

	@Test
	public void testSetFeatureStateExistingInMap() {
		final Feature feature = new NamedFeature("SAMPLE_FEATURE");
		final FeatureState featureState = new FeatureState(feature, true);
		stateRepository.setFeatureState(featureState);
		
		FeatureState storedFeatureState = stateRepository.getFeatureState(feature);
		assertTrue(storedFeatureState.isEnabled());
		assertTrue(EqualsBuilder.reflectionEquals(featureState, storedFeatureState, true));

		featureState.setEnabled(false);
		stateRepository.setFeatureState(featureState);
		storedFeatureState = stateRepository.getFeatureState(feature);
		assertFalse(storedFeatureState.isEnabled());
		
		assertTrue(EqualsBuilder.reflectionEquals(featureState, storedFeatureState, true));
	}
}
