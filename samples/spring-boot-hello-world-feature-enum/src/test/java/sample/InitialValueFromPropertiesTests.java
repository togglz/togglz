package sample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that {@code togglz.features.<NAME>.enabled} properties are used as the default feature
 * state when the StateRepository returns null (e.g. a fresh JDBC database with no rows yet),
 * rather than falling back to the {@code @EnabledByDefault} annotation value.
 *
 * <p>Regression test for https://github.com/togglz/togglz/issues/1184
 */
@SpringBootTest(
        classes = InitialValueFromPropertiesTests.MinimalApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "togglz.feature-enums=sample.Features",
                "togglz.features.HELLO_WORLD.enabled=false",
                "togglz.features.REVERSE_GREETING.enabled=true"
        }
)
class InitialValueFromPropertiesTests {

    @SpringBootApplication(scanBasePackages = "sample.does.not.exist")
    static class MinimalApp {
        @Bean
        StateRepository emptyStateRepository() {
            return new StateRepository() {
                @Override
                public FeatureState getFeatureState(Feature feature) {
                    return null;
                }

                @Override
                public void setFeatureState(FeatureState featureState) {
                }
            };
        }
    }

    @Autowired
    private FeatureManager featureManager;

    @Test
    void propertyEnabledFalseOverridesEnabledByDefaultAnnotation() {
        assertFalse(featureManager.isActive(Features.HELLO_WORLD));
    }

    @Test
    void propertyEnabledTrueUsedWhenFeatureHasNoAnnotation() {
        assertTrue(featureManager.isActive(Features.REVERSE_GREETING));
    }
}
