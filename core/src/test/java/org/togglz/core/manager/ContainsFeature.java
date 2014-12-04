package org.togglz.core.manager;

import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.togglz.core.Feature;

public class ContainsFeature extends BaseMatcher<Set<Feature>> {

	private final Feature feature;

	public ContainsFeature(Feature feature) {
		this.feature = feature;
	}

	@Override
	public boolean matches(Object item) {
		@SuppressWarnings("unchecked")
		Set<Feature> features = (Set<Feature>) item;
		for (Feature f : features) {
            if (f.name().equals(feature.name())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("a set of features containing " + feature);
	}

	public static ContainsFeature containsFeature(Feature feature) {
		return new ContainsFeature(feature);
	}

}
