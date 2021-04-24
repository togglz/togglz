package org.togglz.core.repository.listener;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.util.Weighted;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ListenableStateRepository implements StateRepository {

    private static final Weighted.WeightedComparator WEIGHTED_COMPARATOR = new Weighted.WeightedComparator();
    private final List<FeatureStateChangedListener> listeners;
    private final StateRepository delegate;

    public ListenableStateRepository(final StateRepository delegate,
                                     final FeatureStateChangedListener... listeners) {
        this.delegate = delegate;
        this.listeners = new CopyOnWriteArrayList<>(listeners);
        this.listeners.sort(WEIGHTED_COMPARATOR);
    }

    public final void addFeatureStateChangedListener(final FeatureStateChangedListener listener) {
        listeners.add(listener);
        listeners.sort(WEIGHTED_COMPARATOR);
    }

    @Override
    public FeatureState getFeatureState(final Feature feature) {
        return delegate.getFeatureState(feature);
    }

    @Override
    public void setFeatureState(final FeatureState featureState) {
        final FeatureState fromState = getFeatureState(featureState.getFeature());
        delegate.setFeatureState(featureState);
        listeners.forEach(listener -> listener.onFeatureStateChanged(fromState, featureState));
    }
}
