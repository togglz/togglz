package org.togglz.core;

/**
 * Feature implementation that wraps generic enumeration classes
 *
 * @author rui.figueira
 */
public class GenericEnumFeature implements Feature {

    private final Enum<?> enumValue;

    public GenericEnumFeature(Enum<?> enumValue) {
        this.enumValue = enumValue;
    }

    @Override
    public String name() {
        return enumValue.name();
    }

    public Enum<?> getEnumValue() {
        return enumValue;
    }

    @Override
    public int hashCode() {
        return enumValue.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GenericEnumFeature && enumValue == ((GenericEnumFeature) obj).enumValue;
    }

}