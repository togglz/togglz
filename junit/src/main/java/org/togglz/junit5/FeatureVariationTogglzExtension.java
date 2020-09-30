package org.togglz.junit5;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.ReflectionSupport.newInstance;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.togglz.core.Feature;
import org.togglz.testing.vary.VariationSet;

/**
 * <p>
 *     JUnit Extension to run {@link org.junit.jupiter.api.TestTemplate TestTemplates} with a variation
 *     of enabled and disabled features.
 * </p>
 *
 * @see VaryFeatures
 * @see VariationSetProvider
 *
 * @author Roland Weisleder
 */
class FeatureVariationTogglzExtension implements TestTemplateInvocationContextProvider {

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        Optional<VaryFeatures> varyFeatures = context.getElement().flatMap(ae -> findAnnotation(ae, VaryFeatures.class));
        if (!varyFeatures.isPresent()) {
            return Stream.empty();
        }

        VariationSetProvider variationSetProvider = newInstance(varyFeatures.get().value());
        VariationSet<? extends Feature> variationSet = variationSetProvider.buildVariationSet();

        return variationSet.getVariants().stream()
            .map(enabledFeatures -> new FeatureVariationInvocationContext(variationSet.getFeatureClass(), enabledFeatures));
    }
}
