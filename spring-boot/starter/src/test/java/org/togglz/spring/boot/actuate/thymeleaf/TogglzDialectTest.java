package org.togglz.spring.boot.actuate.thymeleaf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.processor.StandardIfTagProcessor;
import org.thymeleaf.standard.processor.StandardXmlNsTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;
import org.togglz.spring.boot.actuate.thymeleaf.processor.FeatureActiveAttrProcessor;
import org.togglz.spring.boot.actuate.thymeleaf.processor.FeatureInactiveAttrProcessor;

import java.util.ArrayList;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TogglzDialectTest {

    private Set<IProcessor> processors;
    private ArrayList<IProcessor> iProcessors;

    @BeforeEach
    void setUp() {
        String someDialect = "someDialect";
        TogglzDialect togglzDialect = new TogglzDialect();

        processors = togglzDialect.getProcessors(someDialect);
        iProcessors = new ArrayList<>(processors);
    }

    @Test
    void shouldContainThreeProcessors() {
        assertEquals(3, processors.size());
    }

    @Test
    void shouldContainFeatureActiveProcessor() {
        IProcessor iProcessor = iProcessors.get(0);
        assertTrue(iProcessor instanceof FeatureActiveAttrProcessor);
        assertEquals(iProcessor.getPrecedence(), StandardIfTagProcessor.PRECEDENCE);
        assertEquals(iProcessor.getTemplateMode(), TemplateMode.HTML);
    }

    @Test
    void shouldContainFeatureInActiveProcessor() {
        IProcessor iProcessor = iProcessors.get(1);
        assertTrue(iProcessor instanceof FeatureInactiveAttrProcessor);
        assertEquals(iProcessor.getPrecedence(), StandardIfTagProcessor.PRECEDENCE);
        assertEquals(iProcessor.getTemplateMode(), TemplateMode.HTML);
    }

    @Test
    void shouldContainStandardXmlNsTagProcessor() {
        IProcessor iProcessor = iProcessors.get(2);
        assertTrue(iProcessor instanceof StandardXmlNsTagProcessor);
        assertEquals(iProcessor.getPrecedence(), StandardXmlNsTagProcessor.PRECEDENCE);
        assertEquals(iProcessor.getTemplateMode(), TemplateMode.HTML);
    }
}