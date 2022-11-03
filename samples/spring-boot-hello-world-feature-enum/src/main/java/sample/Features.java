package sample;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.FeatureGroup;
import org.togglz.core.annotation.Label;

public enum Features implements Feature {

    @Label("just a description")
    @FeatureGroup("zwei")
    @EnabledByDefault
    HELLO_WORLD,

    @Label("another description")
    @FeatureGroup("eins")
    REVERSE_GREETING;
}

