package org.togglz.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public class S3StateRepositoryTest {
    @SuppressWarnings("serial")
    @Test
    public void test() {
        AmazonS3Client client = new AmazonS3ClientMOCK(new AnonymousAWSCredentials());
        client.withEndpoint(String.format("http://localhost:8001"));
        client.createBucket("testbucket");

        S3StateRepository repository = S3StateRepository.newBuilder(client, "testbucket").build();

        assertNull(repository.getFeatureState(TestFeature.FEATURE_1));

        FeatureState initFs = new FeatureState(TestFeature.FEATURE_1)
            .setEnabled(true)
            .setStrategyId("abc")
            .setParameter("key1", "value1");

        repository.setFeatureState(initFs);

        FeatureState actualFs = repository.getFeatureState(TestFeature.FEATURE_1);

        assertThat(actualFs.getFeature()).isEqualTo(initFs.getFeature());
        assertThat(actualFs.getStrategyId()).isEqualTo("abc");
        assertThat(actualFs.isEnabled()).isEqualTo(true);
        assertThat(actualFs.getParameter("key1")).isEqualTo("value1");
        assertThat(actualFs.getParameterNames()).isEqualTo(new HashSet<String>() {
            {
                add("key1");
            }
        });

        FeatureState updatedFs = new FeatureState(TestFeature.FEATURE_1)
            .setEnabled(false)
            .setStrategyId("def")
            .setParameter("key2", "value2");

        repository.setFeatureState(updatedFs);

        actualFs = repository.getFeatureState(TestFeature.FEATURE_1);

        assertThat(actualFs.getFeature()).isEqualTo(initFs.getFeature());
        assertThat(actualFs.getStrategyId()).isEqualTo("def");
        assertThat(actualFs.isEnabled()).isEqualTo(false);
        assertThat(actualFs.getParameter("key2")).isEqualTo("value2");
        assertThat(actualFs.getParameterNames()).isEqualTo(new HashSet<String>() {
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
