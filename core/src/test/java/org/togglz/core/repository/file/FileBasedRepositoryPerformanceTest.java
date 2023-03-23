package org.togglz.core.repository.file;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

class FileBasedRepositoryPerformanceTest {

    private File file;
    private FileBasedStateRepository repository;
    private static final Logger log = LoggerFactory.getLogger(FileBasedRepositoryPerformanceTest.class);

    @BeforeEach
    void before() throws Exception {

        // create repository
        file = File.createTempFile(this.getClass().getSimpleName(), null);
        repository = new FileBasedStateRepository(file);

        // configure the state for EXISTING
        repository.setFeatureState(new FeatureState(PerformanceFeatures.EXISTING).enable());

        // read it once for warming up the cache
        repository.getFeatureState(PerformanceFeatures.EXISTING);

    }

    @AfterEach
    void after() {
        file.delete();
    }

    @Test
    void readingExistingFeature() {
        runPerformanceTest(PerformanceFeatures.EXISTING);
    }

    @Test
    void readingMissingFeature() {
        runPerformanceTest(PerformanceFeatures.MISSING);
    }

    private void runPerformanceTest(Feature feature) {

        for (int l = 0; l < 6; l++) {

            long start = System.currentTimeMillis();
            for (int i = 0; i < 100000; i++) {
                repository.getFeatureState(feature);
            }
            long time = System.currentTimeMillis() - start;

            log.info("Time for " + feature.name() + ": " + time);
        }
    }

    private enum PerformanceFeatures implements Feature {
        EXISTING,
        MISSING;
    }
}
