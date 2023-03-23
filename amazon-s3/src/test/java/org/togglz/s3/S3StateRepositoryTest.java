package org.togglz.s3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class S3StateRepositoryTest {

    private S3StateRepository repository;

    private FeatureState initState;

    @BeforeEach
    public void setup() {
        S3Client client = new AmazonS3ClientMOCK();
        client.createBucket(CreateBucketRequest.builder().bucket("testbucket").build());

        repository = S3StateRepository.newBuilder(client, "testbucket").build();

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
