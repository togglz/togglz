package org.togglz.benchmark;

import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

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
import org.togglz.core.proxy.FeatureProxyInvocationHandler;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;

/**
 * Compare the overhead of togglz JDK Proxy with an in-memory state repository vs a direct call
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Measurement(batchSize = 1000, iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Warmup(batchSize = 1000, iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Threads(7)
public class JdkProxyBenchmark {

  private static final Supplier<String> sayHello = () -> "Hello";
  private static final Supplier<String> sayWorld = () -> "World";

  Supplier<String> proxy;
  FeatureManager featureManager;

  private enum ProxyFeature implements Feature {
    ENABLED;

    public boolean isActive() {
      return FeatureContext.getFeatureManager().isActive(this);
    }
  }

  @SuppressWarnings("unchecked")
  @Setup(Level.Trial)
  public void setupFeatureManager() {

    // create an in-memory state repository for our feature
    featureManager = new FeatureManagerBuilder()
      .featureEnums(ProxyFeature.class)
      .stateRepository(new InMemoryStateRepository())
      .userProvider(new NoOpUserProvider())
      .build();
    // set the StaticFeatureManagerProvider to use this feature manager
    StaticFeatureManagerProvider.setFeatureManager(featureManager);

    // Create JDK proxy
    proxy = (Supplier<String>)Proxy.newProxyInstance(
      this.getClass().getClassLoader(),
      new Class[] {Supplier.class},
      new FeatureProxyInvocationHandler(ProxyFeature.ENABLED, sayHello, sayWorld, featureManager)
    );

    featureManager.setFeatureState(new FeatureState(ProxyFeature.ENABLED, true));
  }

  @Benchmark
  public String proxyCall() {
    return proxy.get();
  }

  @Benchmark
  public String directCall() {
    return sayHello.get();
  }

  // run this method to execute this test
  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
      .include(JdkProxyBenchmark.class.getSimpleName())
      .forks(1)
      .build();

    new Runner(opt).run();
  }

}
