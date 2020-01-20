package org.togglz.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.file.FileBasedStateRepository;

import java.io.File;
import java.io.IOException;

/**
 * Basic benchmark of the performance of the PropertyBasedStateRepository
 *
 * @author Ryan Gardner
 * @date 5/26/16
 */
public class PropertyBasedStateRepositoryBenchmark extends AbstractStateRepositoryBenchmark {

    private File tempFile;

    @Override
    public StateRepository initializeStateRepository() throws IOException {
        tempFile = File.createTempFile(this.getClass().getSimpleName(), null);
        StateRepository stateRepository = new FileBasedStateRepository(tempFile);
        return stateRepository;
    }

    @Override
    public void cleanupStateRepository() {
        tempFile.delete();
    }

    // run this method to execute this test
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(PropertyBasedStateRepositoryBenchmark.class.getSimpleName())
                .forks(2)
                .build();

        new Runner(opt).run();
    }
}
