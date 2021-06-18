package org.togglz.spock;

import java.lang.reflect.Parameter;

import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.MethodInfo;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;
import org.togglz.testing.TestFeatureManager;

public class TogglzInjectionInterceptor implements IMethodInterceptor {
    @Override
    public void intercept(IMethodInvocation invocation) throws Throwable {
        Parameter[] parameters = invocation.getFeature().getFeatureMethod().getReflection().getParameters();
        Object[] arguments = invocation.getArguments();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getType().isAssignableFrom(TestFeatureManager.class)
                    && arguments[i] == MethodInfo.MISSING_ARGUMENT) {
                // Inject from FeatureContext, as spock as no good way to transport state between interceptors
                // and the rest of the code relies on the content of FeatureContext anyway.
                FeatureManager featureManager = FeatureContext.getFeatureManager();
                if (featureManager instanceof TestFeatureManager) {
                    arguments[i] = featureManager;
                } else {
                    throw new IllegalStateException("Expected a TestFeatureManager but got a " +
                            featureManager.getName() + " instead");
                }
            }
        }
        invocation.proceed();
    }
}
