package org.togglz.googlecloudspanner.repository;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.ReadOnlyTransaction;
import com.google.cloud.spanner.TransactionContext;
import com.google.cloud.spanner.TransactionRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SpannerStateRepositoryTest {
    @InjectMocks
    private SpannerStateRepository repo;
    @Mock
    private SpannerFeatureStateDao dao;
    @Mock
    private DatabaseClient databaseClient;
    @Mock
    private ReadOnlyTransaction readOnlyTransaction;
    @Mock
    private FeatureState persistentFeatureState;
    @Mock
    private TransactionRunner readWriteTransactionRunner;
    @Mock
    private TransactionContext readWriteTransaction;

    @Test
    public void shouldConfirmExistenceWhenStatePresent() {
        givenAPersistentFeatureState();

        boolean exists = whenExistenceOfFeatureStateQueried();

        assertTrue(exists);
    }

    @Test
    public void shouldDenyExistenceWhenNoStatePresent() {
        givenNoPersistentFeatureState();

        boolean exists = whenExistenceOfFeatureStateQueried();

        assertFalse(exists);
    }

    @Test
    public void shouldFetchStateWhenPresent() {
        givenAPersistentFeatureState();

        FeatureState featureState = whenFeatureStateQueried();

        thenFeatureStateFound(featureState);
    }

    @Test
    public void shouldReturnNoStateWhenNonePresent() {
        givenNoPersistentFeatureState();

        FeatureState featureStateSelected = whenFeatureStateQueried();

        thenNoFeatureStateFound(featureStateSelected);
    }

    @Test
    public void shouldUpsertOnPersist() {
        givenAPersistableFeatureState();

        whenFeatueStatePersisted();

        thenDaoExecutedUpsert();
    }

    @Test
    public void shouldDeleteOnRemoval() {
        givenARemovableFeatureState();

        whenFeatureStateRemoved();

        thenDaoExecutedDelete();
    }

    private void thenDaoExecutedDelete() {
        verify(dao).delete(TestFeature.F1.name(), readWriteTransaction);
    }

    private void whenFeatureStateRemoved() {
        repo.removeFeatureState(TestFeature.F1);
    }

    private void thenDaoExecutedUpsert() {
        verify(dao).upsert(persistentFeatureState, readWriteTransaction);
    }

    private void whenFeatueStatePersisted() {
        repo.setFeatureState(persistentFeatureState);
    }

    private void givenAPersistableFeatureState() {
        when(databaseClient.readWriteTransaction()).thenReturn(readWriteTransactionRunner);
        when(readWriteTransactionRunner.run(any(TransactionRunner.TransactionCallable.class))).thenAnswer(invocationOnMock -> ((TransactionRunner.TransactionCallable<?>) invocationOnMock.getArgument(0)).run(readWriteTransaction));
    }

    private void givenARemovableFeatureState() {
        when(databaseClient.readWriteTransaction()).thenReturn(readWriteTransactionRunner);
        when(readWriteTransactionRunner.run(any(TransactionRunner.TransactionCallable.class))).thenAnswer(invocationOnMock -> ((TransactionRunner.TransactionCallable<?>) invocationOnMock.getArgument(0)).run(readWriteTransaction));
    }


    private void givenNoPersistentFeatureState() {
        when(databaseClient.readOnlyTransaction()).thenReturn(readOnlyTransaction);
        when(dao.select(TestFeature.F1, readOnlyTransaction)).thenReturn(Optional.empty());
    }

    private void thenNoFeatureStateFound(FeatureState featureStateSelected) {
        assertNull(featureStateSelected);
    }


    private boolean whenExistenceOfFeatureStateQueried() {
        return repo.existsFeatureState(TestFeature.F1);
    }


    private FeatureState whenFeatureStateQueried() {
        return repo.getFeatureState(TestFeature.F1);
    }

    private void givenAPersistentFeatureState() {
        when(databaseClient.readOnlyTransaction()).thenReturn(readOnlyTransaction);
        when(dao.select(TestFeature.F1, readOnlyTransaction)).thenReturn(Optional.of(persistentFeatureState));
    }

    private void thenFeatureStateFound(FeatureState featureStateSelected) {
        assertSame(persistentFeatureState, featureStateSelected);
    }

    private enum TestFeature implements Feature {
        F1;
    }

}
