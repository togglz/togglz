package org.togglz.core.util;

import java.io.Serializable;

import org.togglz.core.Feature;

/**
 * This class can be used if just the name of a feature is known but not the enum type. It is similar to {@link UntypedFeature}
 * but doesn't try to lazily resolve the type when calling {@link #name()}.
 *
 * @author Christian Kaltepoth
 */
public class NamedFeature implements Feature, Serializable {

    private static final long serialVersionUID = 7344455581363755625L;

    private final String name;

    public NamedFeature(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof NamedFeature) ? this.name.equals(((NamedFeature) o).name()) : false;
    }
}
