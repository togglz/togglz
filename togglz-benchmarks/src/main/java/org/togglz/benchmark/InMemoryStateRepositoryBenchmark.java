package org.togglz.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;


/**
 * A benchmark around the "InMemory" state repository.
 * @author Ryan Gardner
 * @date 5/31/16
 */
public class InMemoryStateRepositoryBenchmark extends AbstractStateRepositoryBenchmark {

    @Override
    public StateRepository initializeStateRepository() throws Exception {
        return new InMemoryStateRepository();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(InMemoryStateRepository.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
