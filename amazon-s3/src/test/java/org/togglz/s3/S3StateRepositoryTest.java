package org.togglz.s3;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.util.HashSet;

import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.togglz.s3.S3StateRepository.newBuilder;

@Testcontainers
class S3StateRepositoryTest {
    @RegisterExtension
    public static final S3MockExtension S3_MOCK = S3MockExtension.builder().build();

    private S3StateRepository repository;

    private FeatureState initState;
    private String bucket;
    private S3Client client;

    private static S3Client createS3Client() {
        return S3_MOCK.createS3ClientV2();
    }

    @BeforeEach
    public void setup() {
        bucket = "testbucket" + UUID.randomUUID().toString();
        client = createS3Client();
        client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());

        repository = newBuilder(client, bucket).build();

        initState = new FeatureState(TestFeature.FEATURE_1)
                .setEnabled(true)
                .setStrategyId("abc")
                .setParameter("key1", "value1");
    }

    @Test
    void shouldSetFeatureState() {
        assertNull(repository.getFeatureState(TestFeature.FEATURE_1));

        repository.setFeatureState(initState);

        FeatureState actualState = repository.getFeatureState(TestFeature.FEATURE_1);

        assertEquals(actualState.getFeature(), initState.getFeature());
        assertEquals(actualState.getStrategyId(), "abc");
        assertTrue(actualState.isEnabled());
        assertEquals(actualState.getParameter("key1"), "value1");
        assertEquals(actualState.getParameterNames(), new HashSet<String>() {
            {
                add("key1");
            }
        });
    }

    @Test
    void shouldSetPrefixToEmptyStringWhenNull() {
        repository = newBuilder(client, bucket).prefix(null).build();
        assertEquals("", repository.getKeyPrefix());
    }

    @Test
    void shouldSetPrefixToEmptyStringWhenEmpty() {
        repository = newBuilder(client, bucket).prefix("").build();
        assertEquals("", repository.getKeyPrefix());
    }

    @Test
    void shouldSetPrefix() {
        repository = newBuilder(client, bucket).prefix("some-prefix").build();
        assertEquals("some-prefix", repository.getKeyPrefix());
    }

    @Test
    void shouldSetFeatureStateWithOptionalFields() {
        assertNull(repository.getFeatureState(TestFeature.FEATURE_1));

        repository = newBuilder(client, bucket).sseCustomerAlgorithm("someSseCustomerAlgorithm").sseCustomerKey("someSseCustomerKey").sseCustomerKeyMD5("someCustomerKeyMd5").build();

        repository.setFeatureState(initState);

        FeatureState actualState = repository.getFeatureState(TestFeature.FEATURE_1);

        assertEquals(actualState.getFeature(), initState.getFeature());

        assertEquals("someSseCustomerAlgorithm", repository.getSseCustomerAlgorithm());
        assertEquals("someSseCustomerKey", repository.getSseCustomerKey());
        assertEquals("someCustomerKeyMd5", repository.getSseCustomerKeyMD5());
    }

    @Test
    void shouldUpdateFeatureState() {
        repository.setFeatureState(initState);

        FeatureState actualState = repository.getFeatureState(TestFeature.FEATURE_1);

        assertEquals(actualState.getFeature(), initState.getFeature());

        FeatureState updatedState = new FeatureState(TestFeature.FEATURE_1)
                .setEnabled(false)
                .setStrategyId("def")
                .setParameter("key2", "value2");

        repository.setFeatureState(updatedState);

        actualState = repository.getFeatureState(TestFeature.FEATURE_1);

        assertEquals(actualState.getFeature(), initState.getFeature());
        assertEquals(actualState.getStrategyId(), "def");
        assertFalse(actualState.isEnabled());
        assertEquals(actualState.getParameter("key2"), "value2");
        assertEquals(actualState.getParameterNames(), new HashSet<String>() {
            {
                add("key2");
            }
        });
    }

    private enum TestFeature implements Feature {
        FEATURE_1
    }
}
