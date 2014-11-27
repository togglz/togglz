package org.togglz.appengine.repository;

import javax.inject.Inject;
import javax.inject.Named;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import com.google.appengine.api.NamespaceManager;
import com.google.common.base.Preconditions;

/**
 * Decorates the given StateRepository enforcing its operations to run within a given namespace. 
 * Uses GAE's {@link NamespaceManager} to enforce the given namespace.  
 * 
 * @author Fábio Franco Uechi
 */
public class FixedNamespaceStateRepository implements StateRepository {

    private final String namespace;
    private final StateRepository decorated;

    @Inject
    public FixedNamespaceStateRepository(@Named("togglzFixedNamespace") String namespace, @Named("togglzFixedNamespace") StateRepository decorated) {
        this.decorated = Preconditions.checkNotNull(decorated);
        NamespaceManager.validateNamespace(namespace);
        this.namespace = namespace;
    }

    @Override
    public FeatureState getFeatureState(final Feature feature) {
        return withinNamespace(namespace, new Work<FeatureState> () {
            @Override
            public FeatureState run() {
                return decorated.getFeatureState(feature);
            }
        });
    }

    @Override
    public void setFeatureState(final FeatureState featureState) {
        withinNamespace(namespace, new VoidWork() {
            @Override
            void vrun() {
                decorated.setFeatureState(featureState);
            }
        });
    }

    static interface Work<T> {
        public T run();
    }
    
    static abstract class VoidWork implements Work<Void> {
        @Override
        public Void run() {
            vrun();
            return null;
        }
        abstract void vrun();
    }
    
    public static <R> R withinNamespace(String namespace, Work<R> work) {
        String oldNamespace = NamespaceManager.get();
        NamespaceManager.set(namespace);
        try {
            R r = work.run();
            return r;
        } finally {
            NamespaceManager.set(oldNamespace);
        }
    }
    
}
