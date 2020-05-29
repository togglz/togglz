package org.togglz.s3;

import org.junit.Before;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.utils.IoUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

public class S3StateRepositoryTest {

    private S3Client client;
    private S3StateRepository repository;

    @Before
    public void setup() {
        client = new AmazonS3ClientMOCK();
        client.createBucket(CreateBucketRequest.builder().bucket("testbucket").build());

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

    private static class AmazonS3ClientMOCK implements S3Client {
        Map<String, Map<String, String>> repo = new HashMap<>();

        @Override
        public ResponseInputStream<GetObjectResponse> getObject(GetObjectRequest getObjectRequest) {
            String s3Object = repo.get(getObjectRequest.bucket()).get(getObjectRequest.key());
            if (s3Object == null) {
                InputStream empty = new InputStream() {
                    @Override
                    public int read() {
                        return -1;  // end of stream
                    }
                };
                return new ResponseInputStream<>(GetObjectResponse.builder().build(), AbortableInputStream.create(empty));
            }
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(s3Object.toString().getBytes());
            return new ResponseInputStream<>(GetObjectResponse.builder().build(), AbortableInputStream.create(byteArrayInputStream));
        }

        @Override
        public PutObjectResponse putObject(PutObjectRequest putObjectRequest, RequestBody requestBody) throws AwsServiceException, SdkClientException {
            Map<String, String> r2 = repo.get(putObjectRequest.bucket());

            String s = null;
            try {
                s = IoUtils.toUtf8String(requestBody.contentStreamProvider().newStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (r2.isEmpty()) {
                r2.put(putObjectRequest.key(), s);
                repo.put(putObjectRequest.bucket(), r2);
            } else {
                r2.put(putObjectRequest.key(), s);
            }
            return PutObjectResponse.builder().build();
        }

        @Override
        public CreateBucketResponse createBucket(CreateBucketRequest createBucketRequest) throws BucketAlreadyExistsException, BucketAlreadyOwnedByYouException, AwsServiceException, SdkClientException, S3Exception {
            repo.put(createBucketRequest.bucket(), new HashMap<>());
            return CreateBucketResponse.builder().build();
        }

        @Override
        public String serviceName() {
            return null;
        }

        @Override
        public void close() {

        }
    }
}
