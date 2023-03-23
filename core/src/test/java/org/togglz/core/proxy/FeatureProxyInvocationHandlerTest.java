package org.togglz.core.proxy;

import java.lang.reflect.Proxy;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FeatureProxyInvocationHandlerTest {

  private static final Supplier<String> sayHello = () -> "Hello";
  private static final Supplier<String> sayWorld = () -> "World";
  private FeatureManager featureManager;

  @BeforeEach
  void before() {
    featureManager = new FeatureManagerBuilder()
      .featureEnum(Features.class)
      .stateRepository(new InMemoryStateRepository())
      .userProvider(new NoOpUserProvider())
      .build();

    featureManager.setFeatureState(new FeatureState(Features.F1, true));
  }

  @SuppressWarnings("unchecked")
  @Test
  void jdkProxyListensToFeature() {
    // Given:
    Supplier<String> proxy = (Supplier<String>)Proxy.newProxyInstance(
      this.getClass().getClassLoader(),
      new Class[] {Supplier.class},
      new FeatureProxyInvocationHandler(Features.F1, sayHello, sayWorld, featureManager)
    );
    // When:
    featureManager.setFeatureState(new FeatureState(Features.F1, true));
    assertEquals("Hello", proxy.get());
    featureManager.setFeatureState(new FeatureState(Features.F1, false));
    assertEquals("World", proxy.get());
  }

  private enum Features implements Feature {
    F1
  }

}
