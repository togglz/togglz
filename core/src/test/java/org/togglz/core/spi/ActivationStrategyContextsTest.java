package org.togglz.core.spi;

import org.junit.Test;
import org.togglz.core.activation.Parameter;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;

import static org.junit.Assert.*;

public class ActivationStrategyContextsTest {

    @Test
    public void hasContextReturnsTrueIfInstanceHasMapping() {

        ActivationStrategyContexts contexts = ActivationStrategyContexts.builder()
            .add(TestStrategy.class, null)
            .build();

        assertTrue(contexts.hasContext(TestStrategy.class));

    }

    @Test
    public void hasContextReturnsFalseIfInstanceHasNoMapping() {

        assertFalse(ActivationStrategyContexts.EMPTY.hasContext(TestStrategy.class));

    }

    @Test
    public void getReturnsValueIfInstanceHasMapping() {

        String expectedValue = "expectedValue";

        ActivationStrategyContexts contexts = ActivationStrategyContexts.builder()
            .add(TestStrategy.class, expectedValue)
            .build();

        assertEquals(expectedValue, contexts.get(TestStrategy.class));

    }

    @Test
    public void getReturnsNullIfInstanceHasNoMapping() {

        assertNull(ActivationStrategyContexts.EMPTY.get(TestStrategy.class));

    }

    private static class TestStrategy implements ContextAwareActivationStrategy<Object> {

        public static final String ID = TestStrategy.class.getSimpleName();

        @Override
        public String getId() {
            return ID;
        }

        @Override
        public String getName() {
            return ID;
        }

        @Override
        public boolean isActive(FeatureState featureState, FeatureUser user, Object context) {
            return false;
        }

        @Override
        public Parameter[] getParameters() {
            return new Parameter[0];
        }
    }

}