package org.togglz.googlecloudstorage.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.util.FeatureStateStorageWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * A state repository that uses Google Cloud Storage.
 * <p>
 * The repository is configured using the corresponding {@link Builder}.
 * <p>
 * The bucket you specify must already have been provisioned before you create a repository.
 */
public final class GoogleCloudStorageStateRepository implements StateRepository {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Storage storageClient;
    private final String bucketName;
    private final String prefix;

    private GoogleCloudStorageStateRepository(final Builder builder) {
        this.bucketName = builder.bucketName;
        this.prefix = builder.prefix;
        this.storageClient = builder.storageClient;
    }

    /**
     * Creates a new builder for creating a {@link GoogleCloudStorageStateRepository}.
     *
     * @return a {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public FeatureState getFeatureState(final Feature feature) {
        try {
            Blob blob = storageClient.get(BlobId.of(bucketName, prefix + feature.name()));

            if (blob != null) {
                byte[] content = blob.getContent();

                if (content != null && content.length > 0) {
                    FeatureStateStorageWrapper wrapper = objectMapper.reader()
                            .forType(FeatureStateStorageWrapper.class)
                            .readValue(content);
                    return FeatureStateStorageWrapper.featureStateForWrapper(feature, wrapper);
                }
            }
        } catch (IOException e) {
            throw new GoogleCloudStorageStateRepositoryException("Failed to get the feature state", e);
        }
        return null;
    }

    @Override
    public void setFeatureState(final FeatureState featureState) {
        try {
            FeatureStateStorageWrapper storageWrapper = FeatureStateStorageWrapper.wrapperForFeatureState(featureState);
            String json = objectMapper.writeValueAsString(storageWrapper);

            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, prefix + featureState.getFeature().name())).build();
            storageClient.create(blobInfo, json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new GoogleCloudStorageStateRepositoryException("Failed to set the feature state", e);
        }
    }

    /**
     * Builder for a {@link GoogleCloudStorageStateRepository}.
     */
    public static final class Builder {
        private Storage storageClient;
        private String bucketName;
        private String prefix;

        private Builder() {
            this.prefix = "";
        }

        /**
         * Specifies the cloud storage client.
         *
         * @param storageClient the client instance to use for connecting to google cloud storage.
         * @return a {@link Builder} with current settings.
         */
        public Builder storageClient(final Storage storageClient) {
            this.storageClient = storageClient;
            return this;
        }

        /**
         * Specifies the name of the bucket.
         *
         * @param bucketName Name of the bucket
         * @return a {@link Builder} with current settings.
         */
        public Builder bucketName(final String bucketName) {
            this.bucketName = bucketName;
            return this;
        }

        /**
         * Optional prefix to prepend on to each key.
         *
         * @param prefix The prefix to use.
         * @return a {@link Builder} with current settings.
         */
        public Builder prefix(final String prefix) {
            this.prefix = prefix == null ? "" : prefix;
            return this;
        }

        /**
         * Creates a new {@link GoogleCloudStorageStateRepository} using the current settings.
         */
        public GoogleCloudStorageStateRepository build() {
            return new GoogleCloudStorageStateRepository(this);
        }
    }
}
