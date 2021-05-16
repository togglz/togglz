package org.togglz.benchmark;

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
import org.togglz.core.context.StaticFeatureManagerProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.proxy.ByteBuddyProxyFactory;
import org.togglz.core.proxy.TogglzSwitchable;
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
public class ByteBuddyProxyBenchmark {

  private static final Supplier<String> sayHello = () -> "Hello";
  private static final Supplier<String> sayWorld = () -> "World";

  Supplier<String> passiveProxy;
  Supplier<String> proxy;
  Supplier<String> handCoded;
  Supplier<String> handCoded2;

  FeatureManager featureManager;

  public static final class HandCodedSwitchable extends TogglzSwitchable<Supplier<String>> implements Supplier<String> {
    public HandCodedSwitchable(FeatureManager featureManager, Feature feature, Supplier<String> active, Supplier<String> inactive) {
      super(featureManager, feature, active, inactive);
    }

    public String get() {
      super.checkTogglzState();
      return delegate.get();
    }
  }

  public static final class HandCodedSwitchable2 implements Supplier<String> {
    private final FeatureManager featureManager;
    private final Feature feature;
    private final Supplier<String> active;
    private final Supplier<String> inactive;

    public HandCodedSwitchable2(FeatureManager featureManager, Feature feature, Supplier<String> active, Supplier<String> inactive) {
      this.featureManager = featureManager;
      this.feature = feature;
      this.active = active;
      this.inactive = inactive;
    }

    public String get() {
      return featureManager.isActive(feature) ? active.get() : inactive.get();
    }
  }

  private enum ProxyFeature implements Feature {
    ENABLED
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

    // Create switching proxies for different tests
    proxy = ByteBuddyProxyFactory.proxyFor(
      ProxyFeature.ENABLED, Supplier.class, sayHello, sayWorld, featureManager);
    passiveProxy = ByteBuddyProxyFactory.passiveProxyFor(
      ProxyFeature.ENABLED, Supplier.class, sayHello, sayWorld, featureManager);

    handCoded = new HandCodedSwitchable(
      featureManager, ProxyFeature.ENABLED, sayHello, sayWorld);
    handCoded2 = new HandCodedSwitchable2(featureManager, ProxyFeature.ENABLED, sayHello, sayWorld);

    featureManager.setFeatureState(new FeatureState(ProxyFeature.ENABLED, true));
  }

  @Benchmark
  // Full auto-generated proxy
  public String activeProxy() {
    return proxy.get();
  }

  @Benchmark
  // Full auto-generated passive proxy
  public String passiveProxy() {
    return passiveProxy.get();
  }

  @Benchmark
  // Same design as the auto-generated code to highlight any inefficiency in ByteBuddy solution.
  public String handCodedTogglzSwitchable() {
    return handCoded.get();
  }

  @Benchmark
  // A common style of hand-coded solution
  public String handCodedSwitch() {
    return handCoded2.get();
  }

  @Benchmark
  // Baseline performance of calling the targeted object directly
  public String directCall() {
    return sayHello.get();
  }

  // run this method to execute this test
  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
      .include(ByteBuddyProxyBenchmark.class.getSimpleName())
      .forks(1)
      .build();

    new Runner(opt).run();
  }

}
