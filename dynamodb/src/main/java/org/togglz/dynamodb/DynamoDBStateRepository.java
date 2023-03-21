package org.togglz.dynamodb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.util.FeatureStateStorageWrapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.io.IOException;
import java.util.Map;

/**
 * A state repository that uses Amazon's DynamoDB.
 * <p>
 * The repository is configured using the {@link DynamoDBStateRepositoryBuilder}
 * <p>
 * You must already have a table provisioned before you create this repository.
 */
public class DynamoDBStateRepository implements StateRepository {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DynamoDBStateRepository.class);

    private final ObjectMapper objectMapper;
    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    private final String primaryKeyAttribute;
    public static final String FEATURE_STATE_ATTRIBUTE_NAME = "featureState";

    private DynamoDBStateRepository(DynamoDBStateRepositoryBuilder builder) {
        this.objectMapper = builder.objectMapper;
        this.dynamoDbClient = builder.dynamoDbClient;
        this.tableName = builder.tableName;
        this.primaryKeyAttribute = builder.primaryKey;
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        GetItemRequest request = GetItemRequest.builder()
                .key(Map.of(primaryKeyAttribute, AttributeValue.builder().s(feature.name()).build()))
                .attributesToGet(FEATURE_STATE_ATTRIBUTE_NAME)
                .tableName(tableName)
                .build();

        Map<String, AttributeValue> documentItem = dynamoDbClient.getItem(request).item();
        if (documentItem == null) {
            return null;
        }
        try {
            FeatureStateStorageWrapper wrapper = objectMapper.reader()
                    .forType(FeatureStateStorageWrapper.class)
                    .readValue(documentItem.get(FEATURE_STATE_ATTRIBUTE_NAME).toString());
            return FeatureStateStorageWrapper.featureStateForWrapper(feature, wrapper);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't parse the feature state", e);
        }
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        try {
            String json = objectMapper.writeValueAsString(FeatureStateStorageWrapper.wrapperForFeatureState(featureState));
            UpdateItemRequest request = UpdateItemRequest.builder()
                    .tableName(tableName)
                    .key(Map.of(primaryKeyAttribute, AttributeValue.builder()
                            .s(featureState.getFeature().name())
                            .build()))
                    .attributeUpdates(Map.of(FEATURE_STATE_ATTRIBUTE_NAME, AttributeValueUpdate.builder()
                            .value(AttributeValue.builder()
                                    .s(json)
                                    .build())
                            .build()))
                    .build();
            dynamoDbClient.updateItem(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to serialize the feature state", e);
        }
    }

    /**
     * <p>
     * Builder for a DynamoDBStateRepository.
     * </p>
     * <p>
     * <p>
     * Usage example:
     * </p>
     * <p>
     * <pre>
     * DynamoDBStateRepository dynamoDbStateRepository = new DynamoDBStateRepositoryBuilder(dbClient)
     *     .withStateStoredInTable(&quot;togglz-state-storage&quot;)
     *     .build();
     * </pre>
     */
    public static class DynamoDBStateRepositoryBuilder {
        public static final String DEFAULT_TABLE_NAME = "togglz";
        private String tableName = DEFAULT_TABLE_NAME;
        private final DynamoDbClient dynamoDbClient;
        private ObjectMapper objectMapper;
        private final String primaryKey = "featureName";


        public DynamoDBStateRepositoryBuilder(DynamoDbClient dbClient) {
            this.dynamoDbClient = dbClient;
        }

        public DynamoDBStateRepositoryBuilder withObjectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public DynamoDBStateRepositoryBuilder withStateStoredInTable(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public DynamoDBStateRepository build() {
            if (this.objectMapper == null) {
                this.objectMapper = new ObjectMapper();
            }
            initializeTable();

            return new DynamoDBStateRepository(this);
        }

        private void initializeTable() {
            DescribeTableRequest request = DescribeTableRequest.builder()
                    .tableName(this.tableName)
                    .build();

            try {
                TableDescription tableDescription = dynamoDbClient.describeTable(request).table();
                log.info("Creating DynamoDBStateRepository with table named: {}", tableDescription.tableName());
                log.info("Table description: {}", tableDescription);
            } catch (DynamoDbException e) {
                if (tableName.equals(DEFAULT_TABLE_NAME)) {
                    log.error("The table with the default name '{}' could not be found. You must either create this table, or provide the name of an existing table to the builder to use as the state repository", DEFAULT_TABLE_NAME);
                } else {
                    log.error("The table named '{}' can not be found. Please verify that the table is created in the region you are trying to run before using it to store the togglz state", tableName);
                }
                throw new RuntimeException("The table specified couldn't be found", e);
            } catch (Exception e) {
                log.error("Couldn't describe the table for an unknown reason. Please verify the table exists and you are able to access it", e);
                throw new RuntimeException("Couldn't create a state repository using the supplied table", e);
            }
        }
    }
}
