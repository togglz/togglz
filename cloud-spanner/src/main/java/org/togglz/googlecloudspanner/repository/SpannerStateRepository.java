package org.togglz.googlecloudspanner.repository;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.ReadOnlyTransaction;
import jakarta.inject.Inject;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

/**
 * <p>
 * This repository implementation can be used to store the feature state
 * in <a href="https://https://cloud.google.com/spanner/docs/">Google Cloud Spanner</>
 * </p>
 *
 * <p>
 * {@link SpannerStateRepository} stores the feature state in the FeatureToggle table.
 * </p>
 *
 * @author cbuschka+togglz@gmail.com
 */
public class SpannerStateRepository implements StateRepository {
    private final SpannerFeatureStateDao dao;
    private final DatabaseClient databaseClient;

    @Inject
    public SpannerStateRepository(DatabaseClient databaseClient) {
        this(databaseClient, new DefaultSpannerFeatureStateDao());
    }

    @Inject
    public SpannerStateRepository(DatabaseClient databaseClient, SpannerFeatureStateDao dao) {
        this.databaseClient = databaseClient;
        this.dao = dao;
    }

    public void removeFeatureState(Feature feature) {
        databaseClient.readWriteTransaction().run((tx) -> {
            dao.delete(feature.name(), tx);
            return null;
        });
    }

    public boolean existsFeatureState(Feature feature) {
        try (ReadOnlyTransaction tx = databaseClient.readOnlyTransaction();) {
            return dao.select(feature, tx).isPresent();
        }
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        try (ReadOnlyTransaction tx = databaseClient.readOnlyTransaction()) {
            return dao.select(feature, tx).orElse(null);
        }
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        databaseClient.readWriteTransaction().run((tx) -> {
            dao.upsert(featureState, tx);
            return null;
        });
    }
}
