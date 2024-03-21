package org.togglz.s3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.util.FeatureStateStorageWrapper;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
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
 */
public class S3StateRepository implements StateRepository {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final S3Client client;
    private final String bucketName;
    private final String keyPrefix;

    private final String sseCustomerAlgorithm;
    private final String sseCustomerKey;
    private final String sseCustomerKeyMD5;

    private S3StateRepository(Builder builder) {
        this.client = builder.client;
        this.bucketName = builder.bucketName;
        this.keyPrefix = builder.keyPrefix;
        this.sseCustomerAlgorithm = builder.sseCustomerAlgorithm;
        this.sseCustomerKey = builder.sseCustomerKey;
        this.sseCustomerKeyMD5 = builder.sseCustomerKeyMD5;
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

            if (sseCustomerKey != null) {
                requestBuilder = requestBuilder.sseCustomerKey(sseCustomerKey);
            }

            if (sseCustomerKeyMD5 != null) {
                requestBuilder = requestBuilder.sseCustomerKeyMD5(sseCustomerKeyMD5);
            }

            GetObjectRequest getObjectRequest = requestBuilder.build();

            try (ResponseInputStream<GetObjectResponse> object = client.getObject(getObjectRequest)) {
                if (object != null) {
                    String content = IoUtils.toUtf8String(object);
                    if (!content.isEmpty()) {
                        FeatureStateStorageWrapper wrapper = objectMapper.reader().forType(FeatureStateStorageWrapper.class).readValue(content);
                        return FeatureStateStorageWrapper.featureStateForWrapper(feature, wrapper);
                    }
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

            if (sseCustomerKey != null) {
                requestBuilder = requestBuilder.sseCustomerKey(sseCustomerKey);
            }

            if (sseCustomerKeyMD5 != null) {
                requestBuilder = requestBuilder.sseCustomerKeyMD5(sseCustomerKeyMD5);
            }

            PutObjectRequest putObjectRequest = requestBuilder.build();

            RequestBody requestBody = RequestBody.fromString(json);
            client.putObject(putObjectRequest, requestBody);
        } catch (AwsServiceException | SdkClientException | JsonProcessingException e) {
            throw new RuntimeException("Failed to set the feature state", e);
        }
    }

    String getSseCustomerAlgorithm() {
        return sseCustomerAlgorithm;
    }

    String getSseCustomerKey() {
        return sseCustomerKey;
    }

    String getSseCustomerKeyMD5() {
        return sseCustomerKeyMD5;
    }

    String getKeyPrefix() {
        return keyPrefix;
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
        private String sseCustomerKey;
        private String sseCustomerKeyMD5;

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
         * @return this
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
         * Specifies the customer-provided encryption key for Amazon S3 to use in encrypting data.
         *
         * @param sseCustomerKey – Specifies the customer-provided encryption key for Amazon S3 to use in encrypting data.
         * @return this
         */
        public Builder sseCustomerKey(String sseCustomerKey) {
            this.sseCustomerKey = sseCustomerKey;
            return this;
        }

        /**
         * Specifies the 128-bit MD5 digest of the encryption key according to RFC 1321.
         * Amazon S3 uses this header for a message integrity check to ensure that the encryption key was transmitted without error.
         *
         * @param sseCustomerKeyMD5 – Specifies the 128-bit MD5 digest of the encryption key according to RFC 1321.
         * @return this
         */
        public Builder sseCustomerKeyMD5(String sseCustomerKeyMD5) {
            this.sseCustomerKeyMD5 = sseCustomerKeyMD5;
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
