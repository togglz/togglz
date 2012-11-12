package org.togglz.core.activation;

import java.util.regex.Pattern;

import org.togglz.core.util.Validate;

public class ParameterBuilder implements Parameter {

    private final String id;
    private String name;
    private Pattern pattern;

    public static ParameterBuilder create(String id) {
        return new ParameterBuilder(id);
    }

    private ParameterBuilder(String id) {
        Validate.notNull(id, "id is required");
        this.id = id;
        this.name = id;
    }

    public ParameterBuilder named(String name) {
        Validate.notNull(name, "name is required");
        this.name = name;
        return this;
    }

    public ParameterBuilder matching(String regex) {
        Validate.notNull(regex, "regex is required");
        this.pattern = Pattern.compile(regex);
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(String value) {
        if (pattern != null) {
            return pattern.matcher(value).matches();
        }
        return true;
    }

}
