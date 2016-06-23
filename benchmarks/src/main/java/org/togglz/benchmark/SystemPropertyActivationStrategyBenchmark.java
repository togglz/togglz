package org.togglz.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.togglz.core.Feature;
import org.togglz.core.activation.SystemPropertyActivationStrategy;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.context.StaticFeatureManagerProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;

import java.util.concurrent.TimeUnit;

/**
 * Created by ddcchrisk on 5/26/16.
 */
@State(Scope.Benchmark)
@BenchmarkMode({Mode.Throughput, Mode.SampleTime})
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS, batchSize = 10000)
@Threads(7)
@Warmup(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
public class SystemPropertyActivationStrategyBenchmark {

    FeatureManager manager;

    private enum SystemPropertyActivationStrategyFeatures implements Feature {
        SYSTEM_BASED_FEATURE,
        @EnabledByDefault ALWAYS_ON_FEATURE;
    }

    // create an in-memory state repository for our feature
    @Setup(Level.Trial)
    public void setupFeatureManager() {
        FeatureManager featureManager = new FeatureManagerBuilder()
                .featureEnums(SystemPropertyActivationStrategyFeatures.class)
                .stateRepository(new InMemoryStateRepository())
                .userProvider(new NoOpUserProvider())
                .build();
        // set up the toggle activation state
        StaticFeatureManagerProvider.setFeatureManager(featureManager);
        manager = featureManager;

        FeatureState propertBasedFeatureState = new FeatureState(SystemPropertyActivationStrategyFeatures.SYSTEM_BASED_FEATURE)
            .setEnabled(true)
            .setStrategyId(SystemPropertyActivationStrategy.ID)
            .setParameter(SystemPropertyActivationStrategy.PARAM_PROPERTY_NAME, "foo.bar")
            .setParameter(SystemPropertyActivationStrategy.PARAM_PROPERTY_VALUE, "true");


        manager.setFeatureState(propertBasedFeatureState);


    }

    @Setup(Level.Iteration)
    public void toggleEnabledState() {
        if (System.getProperty("foo.bar") != null) {
            System.clearProperty("foo.bar");
        } else {
            System.setProperty("foo.bar", "true");
        }
    }

    @Benchmark
    public int alwaysOnActivationStrategyBenchMark() {
        return (manager.isActive(SystemPropertyActivationStrategyFeatures.ALWAYS_ON_FEATURE)) ? 0 : 1;
    }

    @Benchmark
    public int systemPropertyActivationStrategyBenchMark() {
        return (manager.isActive(SystemPropertyActivationStrategyFeatures.SYSTEM_BASED_FEATURE)) ? 0 : 1;
    }

    // run this method to execute this test
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SystemPropertyActivationStrategyBenchmark.class.getSimpleName())
                .forks(2)
                .build();

        new Runner(opt).run();
    }

}
