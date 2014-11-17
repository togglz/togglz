package org.togglz.appengine.repository;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

@RunWith(MockitoJUnitRunner.class)
public class FixedNamespaceStateRepositoryTest {

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
        new LocalDatastoreServiceTestConfig());

    private FixedNamespaceStateRepository repository;

    @Mock
    private StateRepository mockedStateRepository;

    private static final String ACME_NAMESPACE = "acme";

    private static final String ORIGINAL_NAMESPACE = "foobar";

    @Before
    public void setup() {
        helper.setUp();
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }

    @Test
    public void shouldAcceptEmptyNamespace() {
        new FixedNamespaceStateRepository("", mockedStateRepository);
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAcceptNullNamespace() {
        new FixedNamespaceStateRepository(null, mockedStateRepository);
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAcceptNullDecorated() {
        new FixedNamespaceStateRepository("", null);
    }

    @Test
    public void getFeatureStateShouldRunWithinGivenNamespace() {
        repository = new FixedNamespaceStateRepository(ACME_NAMESPACE, new StateRepository() {
            @Override
            public void setFeatureState(FeatureState featureState) {
            }

            @Override
            public FeatureState getFeatureState(Feature feature) {
                assertEquals(ACME_NAMESPACE, NamespaceManager.get());
                return null;
            }
        });
        repository.getFeatureState(TestFeature.F1);
    }

    @Test
    public void getFeatureStateShouldRunWithinGivenNamespaceWhenCurrentNamespaceIsNotDefault() {
        NamespaceManager.set(ORIGINAL_NAMESPACE);
        repository = new FixedNamespaceStateRepository(ACME_NAMESPACE, new StateRepository() {
            @Override
            public void setFeatureState(FeatureState featureState) {
            }

            @Override
            public FeatureState getFeatureState(Feature feature) {
                assertEquals(ACME_NAMESPACE, NamespaceManager.get());
                return null;
            }
        });
        repository.getFeatureState(TestFeature.F1);
    }

    @Test
    public void setFeatureStateShouldRunWithinGivenNamespaceWhenCurrentNamespaceIsNotDefault() {
        NamespaceManager.set(ORIGINAL_NAMESPACE);
        repository = new FixedNamespaceStateRepository(ACME_NAMESPACE, new StateRepository() {
            @Override
            public void setFeatureState(FeatureState featureState) {
                assertEquals(ACME_NAMESPACE, NamespaceManager.get());
            }

            @Override
            public FeatureState getFeatureState(Feature feature) {
                return null;
            }
        });

        final FeatureState state = new FeatureState(TestFeature.F1)
            .disable()
            .setStrategyId("someId")
            .setParameter("param", "foo");

        repository.setFeatureState(state);
    }

    @Test
    public void setFeatureStateShouldRunWithinGivenNamespace() {
        repository = new FixedNamespaceStateRepository(ACME_NAMESPACE, new StateRepository() {
            @Override
            public void setFeatureState(FeatureState featureState) {
                assertEquals(ACME_NAMESPACE, NamespaceManager.get());
            }

            @Override
            public FeatureState getFeatureState(Feature feature) {
                return null;
            }
        });

        final FeatureState state = new FeatureState(TestFeature.F1)
            .disable()
            .setStrategyId("someId")
            .setParameter("param", "foo");

        repository.setFeatureState(state);
    }

    @Test
    public void shouldFallbackToOriginalNamespace() {
        NamespaceManager.set(ORIGINAL_NAMESPACE);
        repository = new FixedNamespaceStateRepository(ACME_NAMESPACE, new StateRepository() {
            @Override
            public void setFeatureState(FeatureState featureState) {
            }

            @Override
            public FeatureState getFeatureState(Feature feature) {
                return null;
            }
        });

        final FeatureState state = new FeatureState(TestFeature.F1)
            .disable()
            .setStrategyId("someId")
            .setParameter("param", "foo");

        repository.setFeatureState(state);
        assertEquals(ORIGINAL_NAMESPACE, NamespaceManager.get());

        repository.getFeatureState(TestFeature.F1);
        assertEquals(ORIGINAL_NAMESPACE, NamespaceManager.get());

    }

    private static enum TestFeature implements Feature {
        F1
    }

}