package org.togglz.spock;

import java.util.List;
import java.util.Objects;

import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.extension.builtin.PreconditionContext;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.testing.TestFeatureManager;
import org.togglz.testing.TestFeatureManagerProvider;

import groovy.lang.Closure;

public class TogglzInterceptor implements IMethodInterceptor {

    private Class<? extends Feature> allEnabled = null;

    private Class<? extends Feature> allDisabled = null;

    private Class<? extends Closure<List<? extends Feature>>> enable = null;

    private Class<? extends Closure<List<? extends Feature>>> disable = null;

    TogglzInterceptor(Togglz specAnnotation, Togglz featureAnnotation) {
        if (specAnnotation == null) {
            assign(validate(Objects.requireNonNull(featureAnnotation,
                    "At least one of specAnnotation and featureAnnotation must be non-null")));
        } else if (featureAnnotation == null) {
            assign(validate(Objects.requireNonNull(specAnnotation,
                    "At least one of specAnnotation and featureAnnotation must be non-null")));
        } else {
            merge(validate(specAnnotation), validate(featureAnnotation));
        }
        if (allEnabled == null && allDisabled == null) {
            throw new IllegalArgumentException("One of allEnabled or allDisabled must be set");
        }
        if (allEnabled != null && enable != null) {
            throw new IllegalArgumentException("You cannot combine allEnable with enable");
        }
        if (allDisabled != null && disable != null) {
            throw new IllegalArgumentException("You cannot combine allDisabled with allDisabled");
        }
    }

    private Togglz validate(Togglz annotation) {
        if (isSet(annotation.allEnabled()) && isSet(annotation.allDisabled())) {
            throw new IllegalArgumentException("Only one of allEnabled or allDisabled must be set");
        }
        return annotation;
    }

    private boolean isNone(Class<?> cls) {
        return Togglz.None.class.equals(cls);
    }

    private boolean isSet(Class<?> cls) {
        return !isNone(cls);
    }

    private void assign(Togglz annotation) {
        assignFeatureClass(annotation);
        assignClosure(annotation);
    }

    private void assignFeatureClass(Togglz annotation) {
        allEnabled = isNone(annotation.allEnabled()) ? null : annotation.allEnabled();
        allDisabled = isNone(annotation.allDisabled()) ? null : annotation.allDisabled();
    }

    private void assignClosure(Togglz annotation) {
        enable = isNone(annotation.enable()) ? null : annotation.enable();
        disable = isNone(annotation.disable()) ? null : annotation.disable();
    }

    private void merge(Togglz specAnnotation, Togglz featureAnnotation) {
        if (isSet(featureAnnotation.allEnabled())) {
            allEnabled = featureAnnotation.allEnabled();
        } else if (isSet(featureAnnotation.allDisabled())) {
            allDisabled = featureAnnotation.allDisabled();
        } else {
            assignFeatureClass(specAnnotation);
        }

        if (isSet(featureAnnotation.enable())) {
            enable = featureAnnotation.enable();
        } else if (isSet(featureAnnotation.disable())) {
            disable = featureAnnotation.disable();
        } else {
            assignClosure(specAnnotation);
        }
    }

    private TestFeatureManager createTestFeatureManager() {
        TestFeatureManager featureManager = null;

        if (allEnabled != null) {
            featureManager = new TestFeatureManager(allEnabled);
            featureManager.enableAll();
        } else if (allDisabled != null) {
            featureManager = new TestFeatureManager(allDisabled);
            featureManager.disableAll();
        }
        return featureManager;
    }

    private void applyEnableDisable(TestFeatureManager featureManager, Object instance) {
        if (enable != null) {
            List<? extends Feature> features = evaluate(enable, instance);
            features.forEach(featureManager::enable);
        } else if (disable != null) {
            List<? extends Feature> features = evaluate(disable, instance);
            features.forEach(featureManager::disable);
        }
    }

    private List<? extends Feature> evaluate(Class<? extends Closure<List<? extends Feature>>> closureClass, Object instance) {
        PreconditionContext preconditionContext = new PreconditionContext();
        Closure<List<? extends Feature>> closure = instantiateClosure(closureClass, instance);
        closure.setDelegate(preconditionContext);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        return closure.call(preconditionContext);
    }

    @Override
    public void intercept(IMethodInvocation invocation) throws Throwable {
        TestFeatureManager testFeatureManager = createTestFeatureManager();
        applyEnableDisable(testFeatureManager, invocation.getInstance());
        TestFeatureManagerProvider.setFeatureManager(testFeatureManager);
        FeatureContext.clearCache();
        try {
            invocation.proceed();
        } finally {
            TestFeatureManagerProvider.setFeatureManager(null);
            FeatureContext.clearCache();
        }
    }

    Closure<List<? extends Feature>> instantiateClosure(Class<? extends Closure<List<? extends Feature>>> closureClass,
            Object instance) {
        try {
            return closureClass.getDeclaredConstructor(Object.class, Object.class).newInstance(instance, null);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not instantiate closure");
        }
    }
}
