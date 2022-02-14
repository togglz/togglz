package org.togglz.googlecloudstorage.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.FeatureStateStorageWrapper;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class GoogleCloudStorageStateRepositToryTest {

    private static final String BUCKET_NAME = "some-bucket";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private GoogleCloudStorageStateRepository repository;
    private Storage storage;

    @BeforeEach
    void setUp() {
        storage = LocalStorageHelper.getOptions().getService();

        repository = GoogleCloudStorageStateRepository.builder()
                .bucketName(BUCKET_NAME)
                .storageClient(storage)
                .build();
    }

    @Test
    void getFeatureState_NotFound() {
        assertNull(repository.getFeatureState(SomeFeature.FEATURE_1));

        FeatureState featureState = repository.getFeatureState(SomeFeature.FEATURE_1);

        assertNull(featureState);
    }

    @Test
    void getFeatureState_NoContent() {
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, SomeFeature.FEATURE_1.name())).build();
        storage.create(blobInfo, new byte[]{});

        FeatureState featureState = repository.getFeatureState(SomeFeature.FEATURE_1);

        assertNull(featureState);
    }

    @Test
    void getFeatureState_BrokenContent() throws JsonProcessingException {
        String json = objectMapper.writeValueAsString("nothing to get here...");
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, SomeFeature.FEATURE_1.name())).build();
        storage.create(blobInfo, json.getBytes(StandardCharsets.UTF_8));

        assertThrows(GoogleCloudStorageStateRepositoryException.class, () -> repository.getFeatureState(SomeFeature.FEATURE_1));
    }

    @Test
    void getFeatureState_FeatureEnabled() throws JsonProcessingException {
        assertNull(repository.getFeatureState(SomeFeature.FEATURE_1));
        setFeature(true);
        assertNotNull(repository.getFeatureState(SomeFeature.FEATURE_1));
        assertTrue(repository.getFeatureState(SomeFeature.FEATURE_1).isEnabled());

        FeatureState featureState = repository.getFeatureState(SomeFeature.FEATURE_1);

        assertNotNull(featureState);
        assertTrue(featureState.isEnabled());
    }

    @Test
    void getFeatureState_FeatureDisabled() throws JsonProcessingException {
        assertNull(repository.getFeatureState(SomeFeature.FEATURE_1));
        setFeature(false);
        assertNotNull(repository.getFeatureState(SomeFeature.FEATURE_1));
        assertFalse(repository.getFeatureState(SomeFeature.FEATURE_1).isEnabled());

        FeatureState featureState = repository.getFeatureState(SomeFeature.FEATURE_1);

        assertNotNull(featureState);
        assertFalse(featureState.isEnabled());
    }

    @Test
    void setFeatureState_setsInitialState() {
        assertNull(repository.getFeatureState(SomeFeature.FEATURE_1));

        repository.setFeatureState(new FeatureState(SomeFeature.FEATURE_1, true));

        FeatureState result = repository.getFeatureState(SomeFeature.FEATURE_1);
        assertNotNull(result);
        assertTrue(result.isEnabled());
    }

    @Test
    void setFeatureState_flipsState() throws JsonProcessingException {
        setFeature(true);

        repository.setFeatureState(new FeatureState(SomeFeature.FEATURE_1, false));

        FeatureState result = repository.getFeatureState(SomeFeature.FEATURE_1);
        assertNotNull(result);
        assertFalse(result.isEnabled());
    }

    @Test
    void getState() {
        FeatureState initialState = new FeatureState(SomeFeature.FEATURE_1)
                .setEnabled(true)
                .setStrategyId("some-strategy")
                .setParameter("some-key", "some-value");
        repository.setFeatureState(initialState);

        FeatureState actualState = repository.getFeatureState(SomeFeature.FEATURE_1);

        assertEquals(initialState.getFeature(), actualState.getFeature());
        assertTrue(actualState.isEnabled());
        assertEquals("some-strategy", actualState.getStrategyId());
        assertEquals("some-value", actualState.getParameter("some-key"));
    }

    @Test
    void updateState() {
        FeatureState initialState = new FeatureState(SomeFeature.FEATURE_1)
                .setEnabled(true)
                .setStrategyId("some-strategy")
                .setParameter("some-key", "some-value");
        repository.setFeatureState(initialState);
        FeatureState actualState = repository.getFeatureState(SomeFeature.FEATURE_1);
        assertEquals(actualState.getFeature(), initialState.getFeature());
        FeatureState updatedState = new FeatureState(SomeFeature.FEATURE_1)
                .setEnabled(false)
                .setStrategyId("some-other-strategy")
                .setParameter("some-other-key", "some-other-value");
        repository.setFeatureState(updatedState);

        actualState = repository.getFeatureState(SomeFeature.FEATURE_1);

        assertEquals(initialState.getFeature(), actualState.getFeature());
        assertFalse(actualState.isEnabled());
        assertEquals("some-other-strategy", actualState.getStrategyId());
        assertEquals("some-other-value", actualState.getParameter("some-other-key"));
    }

    private void setFeature(boolean enabled) throws JsonProcessingException {
        FeatureStateStorageWrapper featureStateStorageWrapper = FeatureStateStorageWrapper.wrapperForFeatureState(new FeatureState(SomeFeature.FEATURE_1, enabled));
        String json = objectMapper.writeValueAsString(featureStateStorageWrapper);
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, SomeFeature.FEATURE_1.name())).build();
        storage.create(blobInfo, json.getBytes(StandardCharsets.UTF_8));
    }

    private enum SomeFeature implements Feature {
        FEATURE_1
    }
}
