package org.togglz.dynamodb;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamoDBStateRepositoryIT {
    private static final String PORT = System.getProperty("dynamodb.port");

    private static final Logger log = LoggerFactory.getLogger(DynamoDBStateRepositoryIT.class);

    @Test
    void builderFailsIfTableDoesntExist() {
        log.debug("PORT is {}", PORT);

        DynamoDbClient client = setupAmazonDbClient();

        assertThrows(RuntimeException.class, () -> new DynamoDBStateRepository.DynamoDBStateRepositoryBuilder(client).build());
        client.close();
    }

    @Test
    void testThatPreExistingStateIsUsedWhenItExists() {
        DynamoDbClient client = setupAmazonDbClient();
        new DynamoDBStateRepository.DynamoDBStateRepositoryBuilder(client).withStateStoredInTable("preexistingTable").build();
        client.close();
    }

    private DynamoDbClient setupAmazonDbClient() {
        return DynamoDbClient.builder()
                .credentialsProvider(() -> AwsBasicCredentials.create("not_really_used", "not_really_used"))
                .endpointOverride(URI.create(String.format("http://localhost:%s", PORT)))
                .region(Region.US_EAST_1)
                .build();
    }

    @Test
    void aFeatureStateCanBeSaved() {
        // save some feature state to the db
        DynamoDbClient client = setupAmazonDbClient();
        DynamoDBStateRepository repository = new DynamoDBStateRepository.DynamoDBStateRepositoryBuilder(client)
                .withStateStoredInTable("preexistingTable")
                .build();

        assertNull(repository.getFeatureState(TestFeature.FEATURE));

        FeatureState disabledState = new FeatureState(TestFeature.FEATURE).disable();
        repository.setFeatureState(disabledState);
        assertFalse(repository.getFeatureState(TestFeature.FEATURE).isEnabled());

        FeatureState enabledState = new FeatureState(TestFeature.FEATURE).enable();
        repository.setFeatureState(enabledState);
        assertTrue(repository.getFeatureState(TestFeature.FEATURE).isEnabled());

        repository.setFeatureState(disabledState);
        assertFalse(repository.getFeatureState(TestFeature.FEATURE).isEnabled());
        client.close();
    }

    @Test
    void activationStrategiesCanBeSaved() {
        // save some feature state to the db
        DynamoDbClient client = setupAmazonDbClient();
        DynamoDBStateRepository repository = new DynamoDBStateRepository.DynamoDBStateRepositoryBuilder(client)
                .withStateStoredInTable("preexistingTable")
                .build();

        assertNull(repository.getFeatureState(TestFeature.ANOTHER_FEATURE));

        FeatureState stateWithStrategy = new FeatureState(TestFeature.ANOTHER_FEATURE).enable().setStrategyId("SomeStrategyId").setParameter("SomeParameter", "SomeValue");
        repository.setFeatureState(stateWithStrategy);
        assertTrue(repository.getFeatureState(TestFeature.ANOTHER_FEATURE).isEnabled());
        assertEquals("SomeStrategyId", repository.getFeatureState(TestFeature.ANOTHER_FEATURE).getStrategyId());
        assertEquals("SomeValue", repository.getFeatureState(TestFeature.ANOTHER_FEATURE).getParameter("SomeParameter"));
        client.close();
    }

    private enum TestFeature implements Feature {
        FEATURE,
        ANOTHER_FEATURE
    }
}
