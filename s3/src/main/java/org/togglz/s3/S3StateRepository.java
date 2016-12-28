package org.togglz.s3;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.util.FeatureStateStorageWrapper;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    private ObjectMapper objectMapper = new ObjectMapper();

    protected final AmazonS3Client client;
    protected final String bucketName;
    protected final String keyPrefix;

    private S3StateRepository(Builder builder) {
        this.client = builder.client;
        this.bucketName = builder.bucketName;
        this.keyPrefix = builder.keyPrefix;
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        try (S3Object object = client.getObject(bucketName, keyPrefix + feature.name())) {
            if (object != null) {
                FeatureStateStorageWrapper wrapper = objectMapper.reader().forType(FeatureStateStorageWrapper.class).readValue(object.getObjectContent());
                return FeatureStateStorageWrapper.featureStateForWrapper(feature, wrapper);
            }
        } catch (AmazonS3Exception ae) {
            if (ERR_NO_SUCH_KEY.equals(ae.getErrorCode())) {
                return null;
            }
            throw ae;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get the feature state", e);
        }

        return null;
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        try {
            String json = objectMapper.writeValueAsString(FeatureStateStorageWrapper.wrapperForFeatureState(featureState));
            client.putObject(bucketName, keyPrefix + featureState.getFeature().name(), json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set the feature state", e);
        }
    }

    /**
     * Creates a new builder for creating a {@link S3StateRepository}.
     *
     * @param client the client instance to use for connecting to amazon s3
     * @param bucketName The name of the bucket to use
     * @return a Builder
     */
    public static Builder newBuilder(AmazonS3Client client, String bucketName) {
        return new Builder(client, bucketName);
    }

    /**
     * Builder for a {@link S3StateRepository}.
     */
    public static class Builder {

        private final AmazonS3Client client;
        private final String bucketName;
        private String keyPrefix = "togglz/";

        /**
         * Creates a new builder for a {@link S3StateRepository}.
         *
         * @param client the client instance to use for connecting to amazon s3
         * @param bucketName The name of the bucket to use
         */
        public Builder(AmazonS3Client client, String bucketName) {
            this.client = client;
            this.bucketName = bucketName;
        }

        /**
         * Optional prefixes to prepend on to each key
         * 
         * @param prefix The prefix to use
         * @return
         */
        public Builder prefix(String keyPrefix) {
            this.keyPrefix = keyPrefix == null ? "" : keyPrefix;
            return this;
        }

        /**
         * Creates a new {@link MongoStateRepository} using the current settings.
         */
        public S3StateRepository build() {
            return new S3StateRepository(this);
        }
    }
}
