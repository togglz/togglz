package org.togglz.spring.proxy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.util.NamedFeature;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link FeatureProxyFactoryBean}.
 *
 * @author Jakub Klebek
 */
class FeatureProxyFactoryBeanTest {

    @Test
    @DisplayName("doesn't support null feature")
    void doesntSupportNullFeature() {
        FeatureProxyFactoryBean bean = new FeatureProxyFactoryBean();
        Feature feature = null;
        bean.setFeature(feature);

        assertThrows(IllegalArgumentException.class, bean::afterPropertiesSet);
    }

    @Test
    @DisplayName("doesn't support feature with empty name")
    void doesntSupportFeatureWithEmptyName() {
        FeatureProxyFactoryBean bean = new FeatureProxyFactoryBean();
        Feature feature = new NamedFeature("");
        bean.setFeature(feature);

        assertThrows(IllegalArgumentException.class, bean::afterPropertiesSet);
    }
}
