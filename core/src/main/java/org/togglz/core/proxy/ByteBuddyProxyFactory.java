package org.togglz.core.proxy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import org.togglz.core.Feature;
import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.LazyResolvingFeatureManager;
import static net.bytebuddy.description.type.TypeDescription.Generic.Builder.parameterizedType;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * Produces switching proxy implementations which delegate invocation to one of two objects depending on the state of the specified {@link Feature}.
 * <p>Assuming a provided interface ...
 * <pre>
 * public interface UserDAO { User findById(int id); }
 * </pre>
 * ... this will generate classes equivalent to the following source code ...
 * <pre>
 * public class X extends TogglzSwitchable<UserDAO> implements UserDAO {
 *   public X(FeatureManager featureManager, Feature feature, UserDAO active, UserDAO inactive) {
 *     super(featureManager, feature, active, inactive);
 *   }
 *   public User findById(int id) {
 *     super.checkTogglzState();
 *     return delegate.findById(id);
 *   }
 * }
 * </pre>
 * State is checked as part of <i>every method call</i> which is invisible but also quite invasive.
 * @see TogglzSwitchable
 */
public class ByteBuddyProxyFactory {

  // TODO Nice naming for classes
  // TODO where is the type-caching?
  // TODO active/passive alternatives

  private static final Log log = LogFactory.getLog(ByteBuddyProxyFactory.class);

  public static <T> T proxyFor(Feature feature, Class<? super T> interfaceClass,T active, T inactive) {
    return proxyFor(feature, interfaceClass, active, inactive, new LazyResolvingFeatureManager());
  }

  public static <T> T proxyFor(Feature feature, Class<? super T> interfaceClass, T active, T inactive, FeatureManager featureManager) {
    try {
      Class<?> clazz = generateProxyClass(interfaceClass);

      return (T)clazz.getConstructors()[0].newInstance(featureManager, feature, active, inactive);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create proxy for "+interfaceClass.getSimpleName(), e);
    }
  }

  private static Class<?> generateProxyClass(Class<?> interfaceClass) {
    Class<?> clazz = new ByteBuddy()
      .subclass(parameterizedType(TogglzSwitchable.class, interfaceClass).build())
      .implement(interfaceClass)

      // Define the interface methods excluding any that have default impls.
      .method(isDeclaredBy(interfaceClass).and(not(isDefaultMethod())))
      .intercept(MethodCall.invoke(named("checkTogglzState")).onSuper().andThen(MethodDelegation.toField("delegate")))

      .make()
      .load(Thread.currentThread().getContextClassLoader())
      .getLoaded();

    if( log.isDebugEnabled() ) {
      log.debug("Generated class "+clazz.getName()+" implements "+interfaceClass.getSimpleName());
    }

    return clazz;
  }

}
