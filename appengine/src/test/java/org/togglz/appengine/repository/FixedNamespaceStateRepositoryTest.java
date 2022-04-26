package org.togglz.appengine.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FixedNamespaceStateRepositoryTest {

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
        new LocalDatastoreServiceTestConfig());

    private FixedNamespaceStateRepository repository;

    @Mock
    private StateRepository mockedStateRepository;

    private static final String ACME_NAMESPACE = "acme";

    private static final String ORIGINAL_NAMESPACE = "foobar";

    @BeforeEach
    public void setup() {
        helper.setUp();
    }

    @AfterEach
    public void tearDown() {
        helper.tearDown();
    }

    @Test
    public void shouldAcceptEmptyNamespace() {
        new FixedNamespaceStateRepository("", mockedStateRepository);
    }

    @Test
    public void shouldNotAcceptNullNamespace() {
        assertThrows(NullPointerException.class, () -> {
            new FixedNamespaceStateRepository(null, mockedStateRepository);
        });
    }

    @Test
    public void shouldNotAcceptNullDecorated() {
        assertThrows(NullPointerException.class, () -> {
            new FixedNamespaceStateRepository("", null);
        });
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