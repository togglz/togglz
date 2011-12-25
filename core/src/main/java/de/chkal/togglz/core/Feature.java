package de.chkal.togglz.core;

public interface Feature {

    String name();

    String label();
    
    boolean enabledByDefault();
    
}
