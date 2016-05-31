package org.togglz.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.NoOpUserProvider;

import java.util.concurrent.TimeUnit;

/**
 * @author Ryan Gardner
 * @date 5/31/16
 */
@State(Scope.Benchmark)
@Fork(2)
@BenchmarkMode({Mode.Throughput, Mode.SampleTime})
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS, batchSize = 10000)
@Threads(7)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public abstract class AbstractStateRepositoryBenchmark {


    // a simple feature for this benchmark
    private enum Features implements Feature {
        FEATURE_STATE_SET,
        FEATURE_STATE_NOT_SET;

        public boolean isActive() {
            return FeatureContext.getFeatureManager().isActive(this);
        }
    }

    FeatureManager manager;

    public abstract StateRepository initializeStateRepository() throws Exception;

    // override this if you need to clean stuff up;
    public void cleanupStateRepository() throws Exception {
    }

    @Setup(Level.Trial)
    public void setupFeatureManager() throws Exception {
        StateRepository stateRepository = initializeStateRepository();
        stateRepository.setFeatureState(new FeatureState(Features.FEATURE_STATE_SET, true));

        FeatureManager featureManager = new FeatureManagerBuilder()
                .featureEnums(Features.class)
                .stateRepository(stateRepository)
                .userProvider(new NoOpUserProvider())
                .build();
        manager = featureManager;
    }

    @TearDown(Level.Trial)
    public void teardownStateRepository() throws Exception {
        cleanupStateRepository();
    }

    @Benchmark
    public int stateSetExplicitlyInStateRepository() {
        return (manager.isActive(Features.FEATURE_STATE_SET)) ? 0 : 1;
    }

    @Benchmark
    public int stateNotSet() {
        return (manager.isActive(Features.FEATURE_STATE_NOT_SET)) ? 0 : 1;
    }

}
