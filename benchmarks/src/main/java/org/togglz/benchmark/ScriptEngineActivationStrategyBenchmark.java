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
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.togglz.core.Feature;
import org.togglz.core.activation.ScriptEngineActivationStrategy;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.context.StaticFeatureManagerProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark the performance of the ScriptEngine-based activation strategy in
 * terms of speed and memory footprint
 *
 * @author Ryan Gardner
 * @date 5/13/16
 */
@State(Scope.Benchmark)
@BenchmarkMode({Mode.Throughput, Mode.SampleTime, Mode.SingleShotTime})
@Measurement(iterations = 4, time = 10, timeUnit = TimeUnit.SECONDS)
@Threads(7)
@Warmup(iterations = 4, time = 5, timeUnit = TimeUnit.SECONDS)
public class ScriptEngineActivationStrategyBenchmark {
    FeatureManager manager;

    // a simple feature for this benchmark
    private enum ScriptEngineActivationStrategyFeatures implements Feature {
        ALWAYS_TRUE_SCRIPT_ENGINE_ACTIVATION_STRATEGY,
        DISABLED_FEATURE,
        DYNAMIC_SCRIPT_ENGINE_STRATEGY;


        public boolean isActive() {
            return FeatureContext.getFeatureManager().isActive(this);
        }
    }

    // create an in-memory state repository for our feature
    @Setup(Level.Trial)
    public void setupFeatureManager() {
        FeatureManager featureManager = new FeatureManagerBuilder()
                .featureEnums(ScriptEngineActivationStrategyFeatures.class)
                .stateRepository(new InMemoryStateRepository())
                .userProvider(new NoOpUserProvider())
                .build();
        // set up the toggle activation state
        StaticFeatureManagerProvider.setFeatureManager(featureManager);
        manager = featureManager;

        FeatureState alwaysTrueScriptEngineFeatureState = new FeatureState(ScriptEngineActivationStrategyFeatures.ALWAYS_TRUE_SCRIPT_ENGINE_ACTIVATION_STRATEGY);
        alwaysTrueScriptEngineFeatureState.setEnabled(true);
        alwaysTrueScriptEngineFeatureState.setStrategyId(ScriptEngineActivationStrategy.ID);
        alwaysTrueScriptEngineFeatureState.setParameter(ScriptEngineActivationStrategy.PARAM_LANG, "nashorn");
        alwaysTrueScriptEngineFeatureState.setParameter(ScriptEngineActivationStrategy.PARAM_SCRIPT, "true");

        FeatureState dynamicScriptEngineState = new FeatureState(ScriptEngineActivationStrategyFeatures.DYNAMIC_SCRIPT_ENGINE_STRATEGY);
        dynamicScriptEngineState.setEnabled(true);
        dynamicScriptEngineState.setStrategyId(ScriptEngineActivationStrategy.ID);
        dynamicScriptEngineState.setParameter(ScriptEngineActivationStrategy.PARAM_LANG, "nashorn");
        dynamicScriptEngineState.setParameter(ScriptEngineActivationStrategy.PARAM_SCRIPT, "Math.random() < Math.pow(1 - (27 - date.getDate()) * 0.2, 3)");


        manager.setFeatureState(alwaysTrueScriptEngineFeatureState);
        manager.setFeatureState(dynamicScriptEngineState);
    }

    @Benchmark
    public int scriptEngineThatAlwaysReturnsTrue() {
        return (manager.isActive(ScriptEngineActivationStrategyFeatures.ALWAYS_TRUE_SCRIPT_ENGINE_ACTIVATION_STRATEGY)) ? 0 : 1;
    }

    @Benchmark
    public int scriptEngineDoingDynamicThings() {
        return (manager.isActive(ScriptEngineActivationStrategyFeatures.DYNAMIC_SCRIPT_ENGINE_STRATEGY)) ? 0 : 1;
    }

    @Benchmark
    public int disabledFeatureWithNoScriptEngineInvolved() {
        return (manager.isActive(ScriptEngineActivationStrategyFeatures.DISABLED_FEATURE)) ? 0 : 1;
    }


    // run this method to execute this test
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ScriptEngineActivationStrategyBenchmark.class.getSimpleName())
                .addProfiler(GCProfiler.class)
                .build();

        new Runner(opt).run();
    }



}
