package org.togglz.core.activation;

public interface Parameter {

    String getId();

    String getName();

    boolean isValid(String value);

}
