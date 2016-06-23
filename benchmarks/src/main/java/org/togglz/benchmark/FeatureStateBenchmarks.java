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
import org.togglz.core.activation.ReleaseDateActivationStrategy;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.context.StaticFeatureManagerProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark the performance of using an ActivationStrategy on the performance of a Togglz switch
 *
 * @author Ryan Gardner
 * @date 5/12/16
 */
@State(Scope.Benchmark)
@BenchmarkMode({Mode.Throughput, Mode.SampleTime, Mode.SingleShotTime})
@Measurement(iterations = 8, time = 1, timeUnit = TimeUnit.SECONDS)
@Threads(7)
@Warmup(iterations = 8, time = 1, timeUnit = TimeUnit.SECONDS)
public class FeatureStateBenchmarks {
    FeatureManager manager;
    boolean enabled = false;

    // a simple feature for this benchmark
    private enum ReleaseDateBenchmarkFeature implements Feature {
        RELEASE_DATE_STRATEGY_ENABLED,
        RELEASE_DATE_STRATEGY_DISABLED,
        NO_STATE_CONFIGURED,
        @EnabledByDefault
        ENABLED_BY_DEFAULT,
        DISABLED_BY_FEATURE_STATE;


        public boolean isActive() {
            return FeatureContext.getFeatureManager().isActive(this);
        }
    }

    // create an in-memory state repository for our feature
    @Setup(Level.Trial)
    public void setupFeatureManager() {
        FeatureManager featureManager = new FeatureManagerBuilder()
                .featureEnums(ReleaseDateBenchmarkFeature.class)
                .stateRepository(new InMemoryStateRepository())
                .userProvider(new NoOpUserProvider())
                .build();
        // set up the toggle activation state
        StaticFeatureManagerProvider.setFeatureManager(featureManager);
        manager = featureManager;

        FeatureState releaseDateFeatureState = new FeatureState(ReleaseDateBenchmarkFeature.RELEASE_DATE_STRATEGY_ENABLED);
        releaseDateFeatureState.setEnabled(true);
        releaseDateFeatureState.setStrategyId(ReleaseDateActivationStrategy.ID);
        releaseDateFeatureState.setParameter(ReleaseDateActivationStrategy.PARAM_DATE, "2014-12-31");
        releaseDateFeatureState.setParameter(ReleaseDateActivationStrategy.PARAM_TIME, "12:45:00");

        FeatureState disabledReleaseDate = new FeatureState(ReleaseDateBenchmarkFeature.RELEASE_DATE_STRATEGY_DISABLED);
        disabledReleaseDate.setEnabled(false);
        disabledReleaseDate.setStrategyId(ReleaseDateActivationStrategy.ID);
        disabledReleaseDate.setParameter(ReleaseDateActivationStrategy.PARAM_DATE, "2014-12-31");
        disabledReleaseDate.setParameter(ReleaseDateActivationStrategy.PARAM_TIME, "12:45:00");

        manager.setFeatureState(releaseDateFeatureState);
        manager.setFeatureState(new FeatureState(ReleaseDateBenchmarkFeature.DISABLED_BY_FEATURE_STATE, false));
        manager.setFeatureState(disabledReleaseDate);
    }

    @Benchmark
    public int releaseDateStrategyEnabled() {
        return (manager.isActive(ReleaseDateBenchmarkFeature.RELEASE_DATE_STRATEGY_ENABLED)) ? 0 : 1;
    }

    @Benchmark
    public int releaseDateStrategyConfiguredButNotEnabled() {
        return (manager.isActive(ReleaseDateBenchmarkFeature.RELEASE_DATE_STRATEGY_DISABLED)) ? 0 : 1;
    }

    @Benchmark
    public int noFeatureStateSetBaseline() {
        return (manager.isActive(ReleaseDateBenchmarkFeature.NO_STATE_CONFIGURED)) ? 0 : 1;
    }

    @Benchmark
    public int disabledByAFeatureStateExplicitly() {
        return (manager.isActive(ReleaseDateBenchmarkFeature.DISABLED_BY_FEATURE_STATE)) ? 0 : 1;
    }

    @Benchmark
    public int enabledByDefaultByAnAnnotation() {
        return (manager.isActive(ReleaseDateBenchmarkFeature.ENABLED_BY_DEFAULT)) ? 0 : 1;
    }


    // run this method to execute this test
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FeatureStateBenchmarks.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

}
