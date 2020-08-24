package org.togglz.dynamodb;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ryan Gardner
 * @date 8/29/16
 */
public class DynamoDBStateRepositoryIT {
    private static final String PORT = System.getProperty("dynamodb.port");

    private static final Logger log = LoggerFactory.getLogger(DynamoDBStateRepositoryIT.class);

    @Test
    public void builderFailsIfTableDoesntExist() {
        log.debug("PORT is {}", PORT);

        AmazonDynamoDBClient client = setupAmazonDbClient();

        assertThrows(RuntimeException.class, () -> {
            new DynamoDBStateRepository.DynamoDBStateRepositoryBuilder(client).build();
        });
    }

    @Test
    public void testThatPreExistingStateIsUsedWhenItExists() {
        AmazonDynamoDBClient client = setupAmazonDbClient();
        new DynamoDBStateRepository.DynamoDBStateRepositoryBuilder(client).withStateStoredInTable("preexistingTable").build();
    }

    private AmazonDynamoDBClient setupAmazonDbClient() {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient(
                new BasicAWSCredentials("", "not_really_used")
        );
        client.withEndpoint(String.format("http://localhost:%s", PORT));
        client.setSignerRegionOverride("us-east-1");
        return client;
    }

    @Test
    public void aFeatureStateCanBeSaved() {
        // save some feature state to the db
        AmazonDynamoDBClient client = setupAmazonDbClient();
        DynamoDBStateRepository repository = new DynamoDBStateRepository.DynamoDBStateRepositoryBuilder(client).withStateStoredInTable("preexistingTable").build();

        assertNull(repository.getFeatureState(TestFeature.FEATURE));

        FeatureState disabledState = new FeatureState(TestFeature.FEATURE).disable();
        repository.setFeatureState(disabledState);
        assertFalse(repository.getFeatureState(TestFeature.FEATURE).isEnabled());

        FeatureState enabledState = new FeatureState(TestFeature.FEATURE).enable();
        repository.setFeatureState(enabledState);
        assertTrue(repository.getFeatureState(TestFeature.FEATURE).isEnabled());

        repository.setFeatureState(disabledState);
        assertFalse(repository.getFeatureState(TestFeature.FEATURE).isEnabled());
    }

    @Test
    public void activationStrategiesCanBeSaved() {
        // save some feature state to the db
        AmazonDynamoDBClient client = setupAmazonDbClient();
        DynamoDBStateRepository repository = new DynamoDBStateRepository.DynamoDBStateRepositoryBuilder(client).withStateStoredInTable("preexistingTable").build();

        assertNull(repository.getFeatureState(TestFeature.ANOTHER_FEATURE));

        FeatureState stateWithStrategy = new FeatureState(TestFeature.ANOTHER_FEATURE).enable().setStrategyId("SomeStrategyId").setParameter("SomeParameter", "SomeValue");
        repository.setFeatureState(stateWithStrategy );
        assertTrue(repository.getFeatureState(TestFeature.ANOTHER_FEATURE).isEnabled());
        assertEquals("SomeStrategyId", repository.getFeatureState(TestFeature.ANOTHER_FEATURE).getStrategyId());
        assertEquals("SomeValue", repository.getFeatureState(TestFeature.ANOTHER_FEATURE).getParameter("SomeParameter"));
    }

    private enum TestFeature implements Feature {
        FEATURE,
        ANOTHER_FEATURE
    }
}
