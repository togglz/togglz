package org.togglz.core.proxy;

import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;
import static org.junit.jupiter.api.Assertions.*;

class ByteBuddyProxyFactoryTest {

  private static final Speaker sayHello = new Speaker() { // As an anonymous class
    @Override
    public String getName() {
      return "Hello";
    }

    @Override
    public String toString() {
      return "Active delegate";
    }
  };
  private static final Speaker sayWorld = () -> "World"; // As a lambda

  private FeatureManager featureManager;

  public interface Speaker extends Supplier<String> {
    default String get() { return getName(); }
    String getName();
  }
  public interface Counter extends Supplier<Integer> {
    default Integer get() { return getCount(); }
    int getCount();
  }

  @BeforeEach
  void before() {
    featureManager = new FeatureManagerBuilder()
      .featureEnum(ByteBuddyProxyFactoryTest.Features.class)
      .stateRepository(new InMemoryStateRepository())
      .userProvider(new NoOpUserProvider())
      .build();

    featureManager.setFeatureState(new FeatureState(Features.F1, true));
  }

  @Test
  void byteBuddyProxyHasNiceName() {
    // Given:
    Class<Speaker> interfaceClass = Speaker.class;
    // When:
    Supplier<String> proxy = ByteBuddyProxyFactory.proxyFor(Features.F1, interfaceClass, sayHello, sayWorld, featureManager);
    // Then:
    assertTrue(proxy.getClass().getName().startsWith("org.togglz.core.proxy.ByteBuddyProxyFactoryTest$Speaker$togglz$"));
  }

  @Test
  void byteBuddyProxyDelegatesToString() {
    // Given:
    Class<Speaker> interfaceClass = Speaker.class;
    // When:
    Supplier<String> proxy = ByteBuddyProxyFactory.proxyFor(Features.F1, interfaceClass, sayHello, sayWorld, featureManager);
    // Then:
    assertEquals("Active delegate", proxy.toString());
  }

  @Test
  void byteBuddyProxyListensToFeature() {
    // Given:
    Supplier<String> proxy = ByteBuddyProxyFactory.proxyFor(Features.F1, Supplier.class, sayHello, sayWorld, featureManager);
    // Then:
    featureManager.setFeatureState(new FeatureState(Features.F1, true));
    assertEquals("Hello", proxy.get());
    featureManager.setFeatureState(new FeatureState(Features.F1, false));
    assertEquals("World", proxy.get());
  }

  @Test
  void byteBuddyPassiveProxyDelegatesToString() {
    // Given:
    Supplier<String> proxy = ByteBuddyProxyFactory.passiveProxyFor(Features.F1, Speaker.class, sayHello, sayWorld, featureManager);
    featureManager.setFeatureState(new FeatureState(Features.F1, false));
    assertEquals("Active delegate", proxy.toString());
    // When:
    TogglzSwitchable.update(proxy);
    // Then:
    assertNotEquals("Active delegate", proxy.toString());
  }

  @Test
  void byteBuddyPassiveProxyListensToFeatureOnlyWhenUpdated() {
    // Given:
    Supplier<String> proxy = ByteBuddyProxyFactory.passiveProxyFor(Features.F1, Supplier.class, sayHello, sayWorld, featureManager);
    featureManager.setFeatureState(new FeatureState(Features.F1, false));
    assertEquals("Hello", proxy.get()); // inactive state is ignored
    // When:
    TogglzSwitchable.update(proxy);
    // Then:
    assertEquals("World", proxy.get());
  }

  private enum Features implements Feature {
    F1
  }

}
