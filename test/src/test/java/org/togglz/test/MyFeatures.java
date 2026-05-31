package org.togglz.test;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;

enum MyFeatures implements Feature {

  @EnabledByDefault //
  @Label("First Feature")
  FEATURE_ONE,

  @Label("First Feature")
  FEATURE_TWO

}
