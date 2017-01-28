package org.togglz.s3;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.junit.Before;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

public class S3StateRepositoryTest {
    
    private AmazonS3Client client;
    private S3StateRepository repository;

    @Before
    public void setup() {
        client = new AmazonS3ClientMOCK(new AnonymousAWSCredentials());
        client.withEndpoint(String.format("http://localhost:8001"));
        client.createBucket("testbucket");

        repository = S3StateRepository.newBuilder(client, "testbucket").build();
    }
	
    @SuppressWarnings("serial")
    @Test
    public void testGetSetFeatureState() {
        assertNull(repository.getFeatureState(TestFeature.FEATURE_1));

        FeatureState initState = new FeatureState(TestFeature.FEATURE_1)
            .setEnabled(true)
            .setStrategyId("abc")
            .setParameter("key1", "value1");

        repository.setFeatureState(initState);

        FeatureState actualState = repository.getFeatureState(TestFeature.FEATURE_1);

        assertThat(actualState.getFeature()).isEqualTo(initState.getFeature());
        assertThat(actualState.getStrategyId()).isEqualTo("abc");
        assertThat(actualState.isEnabled()).isEqualTo(true);
        assertThat(actualState.getParameter("key1")).isEqualTo("value1");
        assertThat(actualState.getParameterNames()).isEqualTo(new HashSet<String>() {
            {
                add("key1");
            }
        });
    }

    @SuppressWarnings("serial")
    @Test
    public void testUpdateFeatureState() {
        FeatureState initState = new FeatureState(TestFeature.FEATURE_1)
            .setEnabled(true)
            .setStrategyId("abc")
            .setParameter("key1", "value1");

        repository.setFeatureState(initState);

        FeatureState actualState = repository.getFeatureState(TestFeature.FEATURE_1);

        assertThat(actualState.getFeature()).isEqualTo(initState.getFeature());

        FeatureState updatedState = new FeatureState(TestFeature.FEATURE_1)
            .setEnabled(false)
            .setStrategyId("def")
            .setParameter("key2", "value2");

        repository.setFeatureState(updatedState);

        actualState = repository.getFeatureState(TestFeature.FEATURE_1);

        assertThat(actualState.getFeature()).isEqualTo(initState.getFeature());
        assertThat(actualState.getStrategyId()).isEqualTo("def");
        assertThat(actualState.isEnabled()).isEqualTo(false);
        assertThat(actualState.getParameter("key2")).isEqualTo("value2");
        assertThat(actualState.getParameterNames()).isEqualTo(new HashSet<String>() {
            {
                add("key2");
            }
        });
    }

    private enum TestFeature implements Feature {
        FEATURE_1
    }

    private static class AmazonS3ClientMOCK extends AmazonS3Client {
        Map<String, Map<String, S3Object>> repo = new HashMap<String, Map<String, S3Object>>();

        public AmazonS3ClientMOCK(AWSCredentials awsCredentials) {
            super(awsCredentials);
        }

        @Override
        public S3Object getObject(String bucketName, String key) {
            return repo.get(bucketName).get(key);
        }

        @Override
        public PutObjectResult putObject(String bucketName, String key, String content) {
            Map<String, S3Object> r2 = repo.get(bucketName);

            ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes());
            S3Object s3obj = new S3Object();
            s3obj.setObjectContent(new S3ObjectInputStream(in, null));

            r2.put(key, s3obj);

            return new PutObjectResult();
        }

        @Override
        public Bucket createBucket(String bucketName) {
            repo.put(bucketName, new HashMap<String, S3Object>());

            return new Bucket();
        }
    }
}
