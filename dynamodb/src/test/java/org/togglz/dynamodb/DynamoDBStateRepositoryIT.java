package org.togglz.dynamodb;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Ryan Gardner
 * @date 8/29/16
 */
public class DynamoDBStateRepositoryIT {
    private static final String PORT = System.getProperty("dynamodb.port");

    private static final Logger log = LoggerFactory.getLogger(DynamoDBStateRepositoryIT.class);

    @Test(expected = RuntimeException.class)
    public void builderFailsIfTableDoesntExist() {
        log.debug("PORT is {}", PORT);

        AmazonDynamoDBClient client = setupAmazonDbClient();

        new DynamoDBStateRepository.DynamoDBStateRepositoryBuilder(client).build();
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

        assertThat(repository.getFeatureState(TestFeature.FEATURE), is(nullValue()));

        FeatureState disabledState = new FeatureState(TestFeature.FEATURE).disable();
        repository.setFeatureState(disabledState);
        assertThat(repository.getFeatureState(TestFeature.FEATURE).isEnabled(), is(false));

        FeatureState enabledState = new FeatureState(TestFeature.FEATURE).enable();
        repository.setFeatureState(enabledState);
        assertThat(repository.getFeatureState(TestFeature.FEATURE).isEnabled(), is(true));

        repository.setFeatureState(disabledState);
        assertThat(repository.getFeatureState(TestFeature.FEATURE).isEnabled(), is(false));
    }

    @Test
    public void activationStrategiesCanBeSaved() {
        // save some feature state to the db
        AmazonDynamoDBClient client = setupAmazonDbClient();
        DynamoDBStateRepository repository = new DynamoDBStateRepository.DynamoDBStateRepositoryBuilder(client).withStateStoredInTable("preexistingTable").build();

        assertThat(repository.getFeatureState(TestFeature.ANOTHER_FEATURE), is(nullValue()));

        FeatureState stateWithStrategy = new FeatureState(TestFeature.ANOTHER_FEATURE).enable().setStrategyId("SomeStrategyId").setParameter("SomeParameter", "SomeValue");
        repository.setFeatureState(stateWithStrategy );
        assertThat(repository.getFeatureState(TestFeature.ANOTHER_FEATURE).isEnabled(), is(true));
        assertThat(repository.getFeatureState(TestFeature.ANOTHER_FEATURE).getStrategyId(), is(equalTo("SomeStrategyId")));
        assertThat(repository.getFeatureState(TestFeature.ANOTHER_FEATURE).getParameter("SomeParameter"), is(equalTo("SomeValue")));
    }


    private enum TestFeature implements Feature {
        FEATURE,
        ANOTHER_FEATURE
    }

}
