package org.togglz.s3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.util.FeatureStateStorageWrapper;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * A state repository that uses Amazon S3.
 * <p>
 * The repository is configured using the {@link Builder}
 * <p>
 * You must already have a bucket provisioned before you create this repository.
 *
 * @author Mark Richardson
 * @date 12/27/16
 */
public class S3StateRepository implements StateRepository {

    // http://docs.aws.amazon.com/AmazonS3/latest/API/ErrorResponses.html
    private static final String ERR_NO_SUCH_KEY = "NoSuchKey";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final S3Client client;
    private final String bucketName;
    private final String keyPrefix;

    private final String sseCustomerAlgorithm;

    private S3StateRepository(Builder builder) {
        this.client = builder.client;
        this.bucketName = builder.bucketName;
        this.keyPrefix = builder.keyPrefix;
        this.sseCustomerAlgorithm = builder.sseCustomerAlgorithm;
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        try {
            GetObjectRequest.Builder requestBuilder = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyPrefix + feature.name());

            if (sseCustomerAlgorithm != null) {
                requestBuilder = requestBuilder.sseCustomerAlgorithm(sseCustomerAlgorithm);
            }

            GetObjectRequest getObjectRequest = requestBuilder.build();

            InputStream object = client.getObject(getObjectRequest);
            if (object != null) {
                String content = IoUtils.toUtf8String(object);
                if (!content.isEmpty()) {
                    FeatureStateStorageWrapper wrapper = objectMapper.reader()
                            .forType(FeatureStateStorageWrapper.class)
                            .readValue(content);
                    return FeatureStateStorageWrapper.featureStateForWrapper(feature, wrapper);
                }
            }
        } catch (NoSuchKeyException e) {
            return null;
        } catch (IOException e) {
            throw new RuntimeException("Failed to set the feature state", e);
        }
        return null;
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        try {
            FeatureStateStorageWrapper storageWrapper = FeatureStateStorageWrapper.wrapperForFeatureState(featureState);
            String json = objectMapper.writeValueAsString(storageWrapper);

            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyPrefix + featureState.getFeature().name());

            if (sseCustomerAlgorithm != null) {
                requestBuilder = requestBuilder.sseCustomerAlgorithm(sseCustomerAlgorithm);
            }

            PutObjectRequest putObjectRequest = requestBuilder.build();

            RequestBody requestBody = RequestBody.fromString(json);
            client.putObject(putObjectRequest, requestBody);
        } catch (AwsServiceException | SdkClientException | JsonProcessingException e) {
            throw new RuntimeException("Failed to set the feature state", e);
        }
    }

    /**
     * Creates a new builder for creating a {@link S3StateRepository}.
     *
     * @param client     the client instance to use for connecting to amazon s3
     * @param bucketName The name of the bucket to use
     * @return a Builder
     */
    public static Builder newBuilder(S3Client client, String bucketName) {
        return new Builder(client, bucketName);
    }

    /**
     * Builder for a {@link S3StateRepository}.
     */
    public static class Builder {

        private final S3Client client;
        private final String bucketName;
        private String keyPrefix = "togglz/";

        private String sseCustomerAlgorithm;

        /**
         * Creates a new builder for a {@link S3StateRepository}.
         *
         * @param client     the client instance to use for connecting to amazon s3
         * @param bucketName The name of the bucket to use
         */
        public Builder(S3Client client, String bucketName) {
            this.client = client;
            this.bucketName = bucketName;
        }

        /**
         * Optional prefixes to prepend on to each key
         *
         * @param keyPrefix The prefix to use
         * @return
         */
        public Builder prefix(String keyPrefix) {
            this.keyPrefix = keyPrefix == null ? "" : keyPrefix;
            return this;
        }

        /**
         * Specifies the algorithm to use to when encrypting the object (for example, AES256).
         *
         * @param sseCustomerAlgorithm – Specifies the algorithm to use to when encrypting the object (for example, AES256).
         * @return this
         */
        public Builder sseCustomerAlgorithm(String sseCustomerAlgorithm) {
            this.sseCustomerAlgorithm = sseCustomerAlgorithm;
            return this;
        }

        /**
         * Creates a new {@link S3StateRepository} using the current settings.
         */
        public S3StateRepository build() {
            return new S3StateRepository(this);
        }
    }
}
