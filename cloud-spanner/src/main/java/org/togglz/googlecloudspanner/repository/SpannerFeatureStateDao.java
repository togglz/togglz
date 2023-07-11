package org.togglz.googlecloudspanner.repository;

import com.google.cloud.spanner.ReadOnlyTransaction;
import com.google.cloud.spanner.TransactionContext;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import java.util.Optional;

interface SpannerFeatureStateDao {

    Optional<FeatureState> select(Feature feature, ReadOnlyTransaction tx);

    void upsert(FeatureState featureState, TransactionContext tx);

    void delete(String featureName, TransactionContext tx);
}
