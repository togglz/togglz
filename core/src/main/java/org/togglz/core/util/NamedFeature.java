package org.togglz.core.util;

import org.togglz.core.Feature;

/**
 * This class can be used if just the name of a feature is known but not the enum type. It is similar to {@link UntypedFeature}
 * but doesn't try to lazily resolve the type when calling {@link #name()}.
 * 
 * @author Christian Kaltepoth
 */
public class NamedFeature implements Feature {

    private final String name;

    public NamedFeature(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

}
