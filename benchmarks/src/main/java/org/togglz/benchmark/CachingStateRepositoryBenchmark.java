package org.togglz.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.cache.CachingStateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;

/**
 * Test the performance of the CachingStateRepository when placed in front of an InMemoryStateRepository
 * to see what the overhead of the CachingStateRepository is.
 *
 * @author Ryan Gardner
 * @date 5/31/16
 */
public class CachingStateRepositoryBenchmark extends AbstractStateRepositoryBenchmark {

    @Override
    public StateRepository initializeStateRepository() throws Exception {
        return new CachingStateRepository(new InMemoryStateRepository());
    }

    // run this method to execute this test
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CachingStateRepositoryBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

}
