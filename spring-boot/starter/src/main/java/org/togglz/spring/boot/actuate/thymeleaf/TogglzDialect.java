/*
 * =============================================================================
 *
 *   Copyright (c) 2014, Hendrik Heneke
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */
package org.togglz.spring.boot.actuate.thymeleaf;

import java.util.LinkedHashSet;
import java.util.Set;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;

import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.processor.StandardXmlNsTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;
import org.togglz.spring.boot.actuate.thymeleaf.processor.FeatureActiveAttrProcessor;
import org.togglz.spring.boot.actuate.thymeleaf.processor.FeatureInactiveAttrProcessor;

/**
 * Dialect for Thymeleaf that allows to show/hide DOM containers based on features state. In Order to use it, add an
 * instance of the dialect to your via {@link TemplateEngine}. The dialect uses the default prefix <code>togglz</code>.
 *
 * @author Hendrik Heneke
 * @since 1.0.0
 * @see FeatureActiveAttrProcessor
 * @see FeatureInactiveAttrProcessor
 */
public class TogglzDialect extends AbstractProcessorDialect {

    public TogglzDialect() {
        this("Togglz Dialect", "togglz", StandardDialect.PROCESSOR_PRECEDENCE);
    }

    public TogglzDialect(final String name, final String prefix, final int processorPrecedence) {
        super(name, prefix, processorPrecedence);
    }

    @Override
    public Set<IProcessor> getProcessors(final String dialectPrefix) {
        final Set<IProcessor> processors = new LinkedHashSet<IProcessor>();
        processors.add(new FeatureActiveAttrProcessor(TemplateMode.HTML, dialectPrefix));
        processors.add(new FeatureInactiveAttrProcessor(TemplateMode.HTML, dialectPrefix));
        processors.add(new StandardXmlNsTagProcessor(TemplateMode.HTML, dialectPrefix));
        return processors;
    }

}

