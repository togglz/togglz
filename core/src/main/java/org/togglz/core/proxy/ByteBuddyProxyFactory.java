package org.togglz.core.proxy;

import static net.bytebuddy.description.type.TypeDescription.ForLoadedType.of;
import static net.bytebuddy.description.type.TypeDescription.Generic.Builder.parameterizedType;
import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.isDefaultMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.LazyResolvingFeatureManager;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.NamingStrategy.SuffixingRandom.BaseNameResolver.ForGivenType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;

/**
 * Produces switching proxy implementations which delegate invocation to one of two objects depending on the state of the specified {@link Feature}.
 *
 * <h2>Generated Code</h2>
 * Assuming a user interface ...
 * <pre>
 * public interface UserDAO { User findById(int id); }</pre>
 * ... {@link #proxyFor} will generate classes equivalent to the following source code ...
 * <pre>
 * public class X extends TogglzSwitchable<UserDAO> implements UserDAO {
 *   public X(FeatureManager featureManager, Feature feature, UserDAO active, UserDAO inactive) {
 *     super(featureManager, feature, active, inactive);
 *   }
 *   public User findById(int id) {
 *     super.checkTogglzState();
 *     return delegate.findById(id);
 *   }
 *   public String toString() {
 *     super.checkTogglzState();
 *     return delegate.toString();
 *   }
 * } </pre>
 *
 * <h2>Active and Passive mode</h2>
 * In "active" proxies, {@link Feature} state is checked as part of <i>every method call</i> which is invisible but also
 * quite slow. It may also be functionally dangerous if the implementations are stateful and should not flipped in the
 * middle of a "session".
 * <p>For these cases {@code passiveProxyFor(...)} will generate classes that never check the feature state themselves.
 * To be updated they must be passed to {@link TogglzSwitchable#update(Object)}.
 *
 * <h2>Performance</h2>
 * Following gives approximate invocation overhead of using a {@link Feature}-controlled proxy vs direct calls.
 *
 * <table border="1">
 *   <tr><td>Direct Call</td> <td>100%</td></tr>
 *     <tr><td>Passive ByteBuddy Proxy</td> <td>70%</td></tr>
 *     <tr><td>Active ByteBuddy Proxy</td> <td>15%</td></tr>
 *     <tr><td>JDK Proxy</td> <td>10%</td></tr>
 * </table>
 *
 * @see TogglzSwitchable
 */
public class ByteBuddyProxyFactory {

  // TODO where is the type-caching?

  private static final Logger log = LoggerFactory.getLogger(ByteBuddyProxyFactory.class);

  /**
   * Generate a passive {@link Feature} proxy.
   *
   * @see ByteBuddyProxyFactory
   */
  public static <T> T passiveProxyFor(Feature feature, Class<? super T> interfaceClass, T active, T inactive) {
    return generateProxy(feature, interfaceClass, active, inactive, new LazyResolvingFeatureManager(), true);
  }

  /**
   * Generate a passive {@link Feature} proxy.
   *
   * @see ByteBuddyProxyFactory
   */
  public static <T> T passiveProxyFor(Feature feature, Class<? super T> interfaceClass, T active, T inactive, FeatureManager featureManager) {
    return generateProxy(feature, interfaceClass, active, inactive, featureManager, true);
  }

  /**
   * Generate an active {@link Feature} proxy.
   *
   * @see ByteBuddyProxyFactory
   */
  public static <T> T proxyFor(Feature feature, Class<? super T> interfaceClass, T active, T inactive) {
    return generateProxy(feature, interfaceClass, active, inactive, new LazyResolvingFeatureManager(), false);
  }

  /**
   * Generate an active {@link Feature} proxy.
   *
   * @see ByteBuddyProxyFactory
   */
  public static <T> T proxyFor(Feature feature, Class<? super T> interfaceClass, T active, T inactive, FeatureManager featureManager) {
    return generateProxy(feature, interfaceClass, active, inactive, featureManager, false);
  }

  private static <T> T generateProxy(Feature feature, Class<? super T> interfaceClass, T active, T inactive, FeatureManager featureManager, boolean passiveProxy) {
    try {
      Class<?> clazz = generateProxyClass(interfaceClass, passiveProxy);
      return (T)clazz.getConstructors()[0].newInstance(featureManager, feature, active, inactive);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create proxy for " + interfaceClass.getSimpleName(), e);
    }
  }

  private static Class<?> generateProxyClass(Class<?> interfaceClass, boolean passiveProxy) {
    Implementation.Composable activeProxyImpl = MethodCall.invoke(named("checkTogglzState")).onSuper().andThen(MethodCall.invokeSelf().onField("delegate"));
    Implementation.Composable passiveProxyImpl = MethodCall.invokeSelf().onField("delegate");


    Class<?> clazz = new ByteBuddy()
      .with(new NamingStrategy.SuffixingRandom("togglz", new ForGivenType(of(interfaceClass))))
      .subclass(parameterizedType(TogglzSwitchable.class, interfaceClass).build())
      .implement(interfaceClass)

      // Define the interface methods excluding any that have default impls.
      .method(isDeclaredBy(interfaceClass).and(not(isDefaultMethod()))
                .or(named("toString")))
      .intercept(passiveProxy ? passiveProxyImpl : activeProxyImpl)

      .make()
      .load(Thread.currentThread().getContextClassLoader())
      .getLoaded();

    if (log.isDebugEnabled()) {
      log.debug("Generated class " + clazz.getName() + " implements " + interfaceClass.getSimpleName());
    }

    return clazz;
  }

}
