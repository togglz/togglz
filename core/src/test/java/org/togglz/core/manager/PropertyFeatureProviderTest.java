package org.togglz.core.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import java.util.Set;

import org.assertj.core.api.Condition;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.metadata.FeatureGroup;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.NamedFeature;

public class PropertyFeatureProviderTest {

    @Test
    public void shouldSupportDefinitionWithoutLabel() {

        Properties properties = new Properties();
        properties.setProperty("F1", "");

        PropertyFeatureProvider provider = new PropertyFeatureProvider(properties);

        Set<Feature> features = provider.getFeatures();
        assertThat(features)
            .hasSize(1)
            .areExactly(1, featureNamed("F1"));

        FeatureMetaData metadata = provider.getMetaData(new NamedFeature("F1"));
        assertThat(metadata).isNotNull();
        assertThat(metadata.getLabel()).isEqualTo("F1");
        FeatureState defaultFeatureState = metadata.getDefaultFeatureState();
        assertThat(defaultFeatureState.isEnabled()).isFalse();
        assertThat(metadata.getGroups()).isEmpty();

    }

    @Test
    public void shouldSupportDefinitionWithOnlyLabel() {

        Properties properties = new Properties();
        properties.setProperty("F1", "My Feature");

        PropertyFeatureProvider provider = new PropertyFeatureProvider(properties);

        Set<Feature> features = provider.getFeatures();
        assertThat(features)
            .hasSize(1)
            .areExactly(1, featureNamed("F1"));

        FeatureMetaData metadata = provider.getMetaData(new NamedFeature("F1"));
        assertThat(metadata).isNotNull();
        assertThat(metadata.getLabel()).isEqualTo("My Feature");
        FeatureState defaultFeatureState = metadata.getDefaultFeatureState();
        assertThat(defaultFeatureState.isEnabled()).isFalse();
        assertThat(metadata.getGroups()).isEmpty();

    }

    @Test
    public void shouldSupportDefinitionWithLabelAndDefault() {

        Properties properties = new Properties();
        properties.setProperty("F1", "My Feature;true");

        PropertyFeatureProvider provider = new PropertyFeatureProvider(properties);

        Set<Feature> features = provider.getFeatures();
        assertThat(features)
            .hasSize(1)
            .areExactly(1, featureNamed("F1"));

        FeatureMetaData metadata = provider.getMetaData(new NamedFeature("F1"));
        assertThat(metadata).isNotNull();
        assertThat(metadata.getLabel()).isEqualTo("My Feature");
        FeatureState defaultFeatureState = metadata.getDefaultFeatureState();
        assertThat(defaultFeatureState.isEnabled()).isTrue();
        assertThat(metadata.getGroups()).isEmpty();

    }

    @Test
    public void shouldSupportDefinitionWithLabelAndDefaultAndTrailingSemicolon() {

        Properties properties = new Properties();
        properties.setProperty("F1", "My Feature;true;");

        PropertyFeatureProvider provider = new PropertyFeatureProvider(properties);

        Set<Feature> features = provider.getFeatures();
        assertThat(features)
            .hasSize(1)
            .areExactly(1, featureNamed("F1"));

        FeatureMetaData metadata = provider.getMetaData(new NamedFeature("F1"));
        assertThat(metadata).isNotNull();
        assertThat(metadata.getLabel()).isEqualTo("My Feature");
        FeatureState defaultFeatureState = metadata.getDefaultFeatureState();
        assertThat(defaultFeatureState.isEnabled()).isTrue();
        assertThat(metadata.getGroups()).isEmpty();

    }

    @Test
    public void shouldSupportDefinitionWithSingleGroup() {

        Properties properties = new Properties();
        properties.setProperty("F1", "My Feature;true;Group1");

        PropertyFeatureProvider provider = new PropertyFeatureProvider(properties);

        Set<Feature> features = provider.getFeatures();
        assertThat(features)
            .hasSize(1)
            .areExactly(1, featureNamed("F1"));

        FeatureMetaData metadata = provider.getMetaData(new NamedFeature("F1"));
        assertThat(metadata).isNotNull();
        assertThat(metadata.getLabel()).isEqualTo("My Feature");
        FeatureState defaultFeatureState = metadata.getDefaultFeatureState();
        assertThat(defaultFeatureState.isEnabled()).isTrue();
        assertThat(metadata.getGroups())
            .hasSize(1)
            .areExactly(1, groupNamed("Group1"));

    }

    @Test
    public void canInitializeFromProperties() {

        Properties properties = new Properties();
        properties.setProperty("ID_1", "ID 1;true;Group 1,Group Other");
        properties.setProperty("ID_2", "ID 2;false;Group 2");

        PropertyFeatureProvider provider = new PropertyFeatureProvider(properties);

        Set<Feature> features = provider.getFeatures();

        assertThat(features)
            .hasSize(2)
            .areExactly(1, featureNamed("ID_1"))
            .areExactly(1, featureNamed("ID_2"));

        FeatureMetaData metadata1 = provider.getMetaData(new NamedFeature("ID_1"));
        assertThat(metadata1).isNotNull();
        assertThat(metadata1.getLabel()).isEqualTo("ID 1");
        FeatureState defaultFeatureState1 = metadata1.getDefaultFeatureState();
        assertThat(defaultFeatureState1.isEnabled()).isTrue();
        assertThat(metadata1.getGroups())
            .hasSize(2)
            .areExactly(1, groupNamed("Group 1"))
            .areExactly(1, groupNamed("Group Other"));

        FeatureMetaData metadata2 = provider.getMetaData(new NamedFeature("ID_2"));
        assertThat(metadata2).isNotNull();
        assertThat(metadata2.getLabel()).isEqualTo("ID 2");
        FeatureState defaultFeatureState2 = metadata2.getDefaultFeatureState();
        assertThat(defaultFeatureState2.isEnabled()).isFalse();
        assertThat(metadata2.getGroups())
            .hasSize(1)
            .areExactly(1, groupNamed("Group 2"));

    }

    private Condition<FeatureGroup> groupNamed(final String name) {
        return new Condition<FeatureGroup>() {
            @Override
            public boolean matches(FeatureGroup value) {
                return value != null && value.getLabel().equals(name);
            }
        };
    }

    private Condition<Feature> featureNamed(final String name) {
        return new Condition<Feature>() {
            @Override
            public boolean matches(Feature value) {
                return value != null && value.name().equals(name);
            }
        };
    }

}
