package org.togglz.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class MongoStateRepositoryTest {

    private final Random random = new Random(System.currentTimeMillis());

    static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.2.5");

    @BeforeClass
    public static void beforeClass() {
        mongoDBContainer.start();
    }

    @AfterClass
    public static void afterClass() {
        mongoDBContainer.stop();
    }

    @Test
    public void testInsertAndUpdate() {
        final MongoClient mongoClient = MongoClients.create(mongoDBContainer.getReplicaSetUrl());

        final MongoStateRepository mongoStateRepository = MongoStateRepository.newBuilder(mongoClient, "mongo-state-repository-test").build();

        final FeatureState expectedNullFeatureState = mongoStateRepository.getFeatureState(TestFeature.FEATURE_1);
        assertThat(expectedNullFeatureState).isNull();

        final boolean expectedEnabled = random.nextBoolean();
        final String expectedStrategyId = UUID.randomUUID().toString();
        final String expectedKey1 = "expectedKey1_" + UUID.randomUUID().toString();
        final String expectedValue1 = "expectedValue1_" + UUID.randomUUID().toString();
        final FeatureState initialFeatureState = new FeatureState(TestFeature.FEATURE_1)
                .setEnabled(expectedEnabled)
                .setStrategyId(expectedStrategyId)
                .setParameter(expectedKey1, expectedValue1);

        mongoStateRepository.setFeatureState(initialFeatureState);

        final FeatureState actualFeatureState = mongoStateRepository.getFeatureState(TestFeature.FEATURE_1);

        assertThat(actualFeatureState.getFeature()).isEqualTo(initialFeatureState.getFeature());
        assertThat(actualFeatureState.getStrategyId()).isEqualTo(expectedStrategyId);
        assertThat(actualFeatureState.isEnabled()).isEqualTo(expectedEnabled);
        assertThat(actualFeatureState.getParameter(expectedKey1)).isEqualTo(expectedValue1);
        assertThat(actualFeatureState.getParameterNames()).isEqualTo(new HashSet<String>() {{add(expectedKey1);}});

        final boolean updatedEnabled = !expectedEnabled;
        final String updatedStrategyId = UUID.randomUUID().toString();
        final String expectedKey2 = "expectedKey2_" + UUID.randomUUID().toString();
        final String expectedValue2 = "expectedValue2_" + UUID.randomUUID().toString();
        final FeatureState updatedFeatureState = new FeatureState(TestFeature.FEATURE_1)
                .setEnabled(updatedEnabled)
                .setStrategyId(updatedStrategyId)
                .setParameter(expectedKey2, expectedValue2);

        mongoStateRepository.setFeatureState(updatedFeatureState);

        final FeatureState actualUpdatedFeatureState = mongoStateRepository.getFeatureState(TestFeature.FEATURE_1);

        assertThat(actualUpdatedFeatureState.getFeature()).isEqualTo(updatedFeatureState.getFeature());
        assertThat(actualUpdatedFeatureState.getStrategyId()).isEqualTo(updatedStrategyId);
        assertThat(actualUpdatedFeatureState.isEnabled()).isEqualTo(updatedEnabled);
        assertThat(actualUpdatedFeatureState.getParameter(expectedKey2)).isEqualTo(expectedValue2);
        assertThat(actualUpdatedFeatureState.getParameterNames()).isEqualTo(new HashSet<String>() {{add(expectedKey2);}});

    }

    private enum TestFeature implements Feature {
        FEATURE_1
    }
}
