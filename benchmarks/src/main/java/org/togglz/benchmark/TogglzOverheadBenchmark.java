package org.togglz.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.context.StaticFeatureManagerProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;

import java.util.concurrent.TimeUnit;

/**
 * Compare the overhead of togglz with an in-memory state repository vs a simple boolean flag
 */
@State(Scope.Benchmark)
@BenchmarkMode({Mode.Throughput, Mode.SampleTime, Mode.SingleShotTime})
@Measurement(iterations = 6, time = 1, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Threads(7)
public class TogglzOverheadBenchmark {
    FeatureManager manager;
    boolean enabled = false;

    // a simple feature for this benchmark
    private enum OverheadFeature implements Feature {
        FEATURE;

        public boolean isActive() {
            return FeatureContext.getFeatureManager().isActive(this);
        }
    }

    // create an in-memory state repository for our feature
    @Setup(Level.Trial)
    public void setupFeatureManager() {
        FeatureManager featureManager = new FeatureManagerBuilder()
                .featureEnums(OverheadFeature.class)
                .stateRepository(new InMemoryStateRepository())
                .userProvider(new NoOpUserProvider())
                .build();
        manager = featureManager;
        // set the StaticFeatureManagerProvider to use this feature manager
        StaticFeatureManagerProvider.setFeatureManager(featureManager);
    }

    // toggle the state between iterations to keep the compiler honest
    @Setup(Level.Iteration)
    public void toggleEnabledState() {
        enabled = !enabled;
        manager.setFeatureState(new FeatureState(OverheadFeature.FEATURE, enabled));
    }

    @Benchmark
    public int aSimpleBooleanIfStatement() {
        return (enabled) ? 1 : 0;
    }

    @Benchmark
    public int featureManagerStateLookup() {
        return (manager.isActive(OverheadFeature.FEATURE)) ? 1 : 0;
    }

    @Benchmark
    public int isActiveMethodOnEnum() {
        return (OverheadFeature.FEATURE.isActive()) ? 1 : 0;
    }

    // run this method to execute this test
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(TogglzOverheadBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}
