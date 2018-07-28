package org.togglz.core.util.annotations;

/**
 * Annotations processor class to read annotation on generic enums.
 *
 * @author Rui Figueira
 */
public class GenericEnumAnnotationsProcessor extends AbstractAnnotationsProcessor<Enum<?>> {

    public static final GenericEnumAnnotationsProcessor INSTANCE = new GenericEnumAnnotationsProcessor();

    @Override
    protected String getName(Enum<?> feature) {
        return feature.name();
    }
}
