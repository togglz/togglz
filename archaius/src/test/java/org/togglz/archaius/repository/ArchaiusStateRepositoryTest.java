package org.togglz.archaius.repository;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConcurrentMapConfiguration;
import com.netflix.config.ConfigurationManager;
import org.junit.jupiter.api.*;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import static org.junit.jupiter.api.Assertions.*;

public class ArchaiusStateRepositoryTest {

    private static ConcurrentMapConfiguration mapConfiguration;
    private ArchaiusStateRepository repository;

    @BeforeAll
    public static void setupClass() {

        // creates and installs a concurrent composite configuration which is needed because
        // the Archiaus state repository assumes its use
        mapConfiguration = new ConcurrentMapConfiguration();
        ConcurrentCompositeConfiguration compositeConfiguration = new ConcurrentCompositeConfiguration();
        compositeConfiguration.clear();
        compositeConfiguration.addConfiguration(mapConfiguration);
        ConfigurationManager.install(compositeConfiguration);
    }

    @BeforeEach
    public void setupTest() {

        this.repository = new ArchaiusStateRepository();
    }

    @AfterEach
    public void teardownTest() {

        mapConfiguration.clear();
    }

    @Test
    public void shouldReturnNullWhenStateDoesntExist() {

        final FeatureState state = repository.getFeatureState(TestFeature.F1);

        assertNull(state);
    }

    @Test
    public void shouldReadFalseStateWithoutStrategyAndParameters() {

        addState(TestFeature.F1.name(), false);

        FeatureState state = repository.getFeatureState(TestFeature.F1);

        assertNotNull(state);
        assertEquals(TestFeature.F1, state.getFeature());
        assertFalse(state.isEnabled());
        assertNull(state.getStrategyId());
        assertEquals(0, state.getParameterNames().size());
    }

    @Test
    public void shouldReadTrueStateWithoutStrategyAndParameters() {

        addState(TestFeature.F1.name(), true);

        FeatureState state = repository.getFeatureState(TestFeature.F1);

        /*
         * THEN the properties should be set like expected
         */
        assertNotNull(state);
        assertEquals(TestFeature.F1, state.getFeature());
        assertTrue(state.isEnabled());
        assertNull(state.getStrategyId());
        assertEquals(0, state.getParameterNames().size());
    }

    @Test
    public void withStrategyNoParameters() {

        addState(TestFeature.F1.name(), true, "S1");
        
        FeatureState state = repository.getFeatureState(TestFeature.F1);

        assertNotNull(state);
        assertEquals("S1", state.getStrategyId());
        assertEquals(0, state.getParameterNames().size());
    }

    @Test
    public void withStrategyParameters() {

        addState(TestFeature.F1.name(), true, "S1", new Param("one", "A"), new Param("two", "B"));

        FeatureState state = repository.getFeatureState(TestFeature.F1);

        assertEquals(2, state.getParameterNames().size());
        assertEquals("A", state.getParameter("one"));
        assertEquals("B", state.getParameter("two"));
    }

    @Test
    public void setState() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            repository.setFeatureState(new FeatureState(TestFeature.F1, true));
        });
    }

    private static void addState(String name, boolean enabled) {
        
        mapConfiguration.setProperty(name, Boolean.toString(enabled));
    }

    private static void addState(String name, boolean enabled, String strategyName, Param... params) {
        
        addState(name, enabled);
        mapConfiguration.setProperty(name + ".strategy", strategyName);
        for (Param param : params) {
            mapConfiguration.setProperty(name + ".param." + param.key, param.value);
        }
    }

    private static enum TestFeature implements Feature {
        F1
    }
    
    private static class Param {
        final String key;
        final String value;
        Param(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
