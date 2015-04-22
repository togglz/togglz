package org.togglz.core.repository.composite;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.composite.CompositeStateRepository.IterationOrder;
import org.togglz.core.repository.composite.CompositeStateRepository.SetterSelection;
import org.togglz.core.repository.mem.InMemoryStateRepository;

public class CompositeStateRepositoryTest {

    private StateRepository repo1 = new InMemoryStateRepository();
    private StateRepository repo2 = new InMemoryStateRepository();
    private CompositeStateRepository crepo = new CompositeStateRepository(repo1, repo2);

    @Before
    public void setup() {
        
        repo1 = new InMemoryStateRepository();
        repo2 = new InMemoryStateRepository();
        crepo = new CompositeStateRepository(repo1, repo2);
    }

    @Test
    public void testFeatureNotFound() {
        
        assertNull(crepo.getFeatureState(TestFeature.F1));
    }

    @Test
    public void testGetFeatureInFirstBackingRepo() {
        
        repo1.setFeatureState(new FeatureState(TestFeature.F1, true));
        
        assertTrue(crepo.getFeatureState(TestFeature.F1).isEnabled());
    }

    @Test
    public void testGetFeatureInSecondBackingRepo() {
        
        repo2.setFeatureState(new FeatureState(TestFeature.F1, true));
        
        assertTrue(crepo.getFeatureState(TestFeature.F1).isEnabled());
    }

    @Test
    public void testGetFeatureFIFO() {

        repo1.setFeatureState(new FeatureState(TestFeature.F1, false));
        repo2.setFeatureState(new FeatureState(TestFeature.F1, true));

        assertFalse(crepo.getFeatureState(TestFeature.F1).isEnabled());
    }

    @Test
    public void testGetFeatureLIFO() {

        repo1.setFeatureState(new FeatureState(TestFeature.F1, false));
        repo2.setFeatureState(new FeatureState(TestFeature.F1, true));
        crepo.setIterationOrder(IterationOrder.LIFO);

        assertTrue(crepo.getFeatureState(TestFeature.F1).isEnabled());
    }

    @Test
    public void testSetFeatureLAST() {
        
        crepo.setFeatureState(new FeatureState(TestFeature.F1, true));

        assertNull(repo1.getFeatureState(TestFeature.F1));
        assertTrue(repo2.getFeatureState(TestFeature.F1).isEnabled());
    }

    @Test
    public void testSetFeatureFIRST() {
        
        crepo.setSetterSelection(SetterSelection.FIRST);
        crepo.setFeatureState(new FeatureState(TestFeature.F1, true));

        assertTrue(repo1.getFeatureState(TestFeature.F1).isEnabled());
        assertNull(repo2.getFeatureState(TestFeature.F1));
    }

    enum TestFeature implements Feature {
        
        F1,
        ;
    }
}
