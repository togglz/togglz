package org.togglz.test;

import static java.util.Collections.emptyList;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.metadata.enums.EnumFeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Validate;

/**
 * A {@link FeatureManager} implementation that allows easy manipulation of features in testing environments.
 */
public class TestFeatureManager implements FeatureManager {

  private final Set<String> activeFeatures;

  private final Set<Class<? extends Feature>> featureEnums;

  private final Set<Feature> features;

  public TestFeatureManager() {

    this.activeFeatures = new HashSet<>();
    this.featureEnums = new HashSet<>();
    this.features = new HashSet<>();
  }

  public void addFeatureEnum(Class<? extends Feature> featureEnum) {

    if (this.featureEnums.contains(featureEnum)) {
      return;
    }
    Objects.requireNonNull(featureEnum, "The featureEnum argument is required");
    Validate.isTrue(featureEnum.isEnum(), "This feature manager currently only works with feature enums");
    for (Feature feature : featureEnum.getEnumConstants()) {
      this.features.add(feature);
      try {
        String name = feature.name();
        Field field = featureEnum.getField(name);
        if (field.isAnnotationPresent(EnabledByDefault.class)) {
          this.activeFeatures.add(name);
        }
      } catch (NoSuchFieldException ignored) {
      }
    }
    this.featureEnums.add(featureEnum);
  }

  @Override
  public String getName() {

    return this.getClass().getSimpleName();
  }

  @Override
  public Set<Feature> getFeatures() {

    return this.features;
  }

  @Override
  public FeatureMetaData getMetaData(Feature feature) {

    return new EnumFeatureMetaData(feature);
  }

  @Override
  public boolean isActive(Feature feature) {

    addFeatureEnum(feature.getClass());
    return this.activeFeatures.contains(feature.name());
  }

  @Override
  public FeatureUser getCurrentFeatureUser() {

    return null;
  }

  @Override
  public FeatureState getFeatureState(Feature feature) {

    return new FeatureState(feature, isActive(feature));
  }

  @Override
  public void setFeatureState(FeatureState state) {

    if (state.isEnabled()) {
      this.activeFeatures.add(state.getFeature().name());
    } else {
      this.activeFeatures.remove(state.getFeature().name());
    }
  }

  @Override
  public List<ActivationStrategy> getActivationStrategies() {

    return emptyList();
  }

  @Override
  public void enable(Feature feature) {

    this.activeFeatures.add(feature.name());
  }

  @Override
  public void disable(Feature feature) {

    this.activeFeatures.remove(feature.name());
  }

  public void enableAll() {

    for (Feature feature : this.features) {
      enable(feature);
    }
  }

  public void disableAll() {

    this.activeFeatures.clear();
  }

}
