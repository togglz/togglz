package de.chkal.togglz.core;

public interface Feature {

    String name();

    boolean isEnabled();
    
    String label();
    
    boolean enabledByDefault();
    
}
