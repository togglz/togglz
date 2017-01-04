package org.togglz.core.repository.composite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;

/**
 * A {@link StateRepository} that is composed of one or more underlying
 * repositories. For {@link #getFeatureState(Feature)} this implementation
 * returns the first non-null value from the underlying repositories, searched
 * in the order specified during construction. The
 * {@link #setFeatureState(FeatureState)} method calls the corresponding method
 * on the last underlying repository.
 * 
 * If you don't want {@link #setFeatureState(FeatureState)} to update a
 * persistent repository, consider using an {@link InMemoryStateRepository} as
 * the last argument. *
 */
public class CompositeStateRepository implements StateRepository {

    private final List<StateRepository> repositories;
    private RepositorySelector iterationOrder = IterationOrder.FIFO;
    private RepositorySelector setterSelection = SetterSelection.LAST;

    /**
     * Creates a composite state repository using the specified underlying state
     * repositories.
     * 
     * @param repositories state repositories
     */
    public CompositeStateRepository(StateRepository... repositories) {

        this.repositories = Arrays.asList(repositories);
    }

    /**
     * Sets the order this composite calls to get feature states.  If not set the default iteration order
     * is first-in-first-out.
     * 
     * @param iterationOrder the iteration order
     * @see IterationOrder
     */
    public void setIterationOrder(RepositorySelector iterationOrder) {
        
        this.iterationOrder = iterationOrder;
    }

    /**
     * Sets the selector for which state repositories to call to set a feature state.  If not set the default
     * is the last repository in this composite.
     * 
     * @param setterSelection the selector
     * @see SetterSelection
     */
    public void setSetterSelection(RepositorySelector setterSelection) {
        
        this.setterSelection = setterSelection;
    }

    /**
     * Returns the first non-null feature state as determined by the current iteration order.
     * 
     * @see #setIterationOrder(RepositorySelector)
     */
    @Override
    public FeatureState getFeatureState(Feature feature) {

        for (StateRepository repository : iterationOrder.getSelected(repositories)) {
            FeatureState featureState = repository.getFeatureState(feature);
            if (featureState != null) {
                return repository.getFeatureState(feature);
            }
        }
        
        return null;
    }

    /**
     * Sets the feature state on the repositories returned by the current setter selection.
     * 
     * @see #setSetterSelection(RepositorySelector)
     */
    @Override
    public void setFeatureState(FeatureState featureState) {

        for (StateRepository repository : setterSelection.getSelected(repositories)) {
            repository.setFeatureState(featureState);
        }
    }
    
    /**
     * Provides a means to select from a collection of state repositories.
     */
    public interface RepositorySelector {
        
        /**
         * Returns a subset of state repositories from the specified collection.  The order in the returned collection
         * may be different than the initial order.
         * 
         * @param from the collection to select from
         * @return the selected state repositories
         */
        List<StateRepository> getSelected(List<StateRepository> from);
    }
    
    public static enum IterationOrder implements RepositorySelector {
        
        /**
         * The iteration order is the same as what was specified when constructing this composite.
         */
        FIFO {
            public List<StateRepository> getSelected(List<StateRepository> from) {
                return new ArrayList<StateRepository>(from);
            }
        },
        
        /**
         * The iteration order is the revers of what was specified when constructing this composite.
         */
        LIFO {
            public List<StateRepository> getSelected(List<StateRepository> from) {
                List<StateRepository> result = new ArrayList<StateRepository>(from);
                Collections.reverse(result);
                return result;
            }
        },
        
        ;
    }
    
    public static enum SetterSelection implements RepositorySelector {
        
        /**
         * Use the first repository in this composite to set the state.
         */
        FIRST {
            public List<StateRepository> getSelected(List<StateRepository> from) {
                return get(from, 0);
            }
        },
        
        /**
         * Use the last repository in this composite to set the state.
         */
        LAST {
            public List<StateRepository> getSelected(List<StateRepository> from) {
                return get(from, from.size() - 1);
            }
        },
        
        /**
         * Use all repositories in this composite to set the state.
         */
        ALL {
            public List<StateRepository> getSelected(List<StateRepository> from) {
                return from;
            }
        };
        
        private static List<StateRepository> get(List<StateRepository> from, int index) {
            List<StateRepository> result = new ArrayList<StateRepository>(1);
            result.add(from.get(index));
            return result;
        }
    }
}
