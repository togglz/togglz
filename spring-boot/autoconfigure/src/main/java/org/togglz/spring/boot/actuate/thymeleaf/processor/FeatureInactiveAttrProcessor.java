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
package org.togglz.spring.boot.actuate.thymeleaf.processor;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.standard.processor.StandardIfTagProcessor;

import org.thymeleaf.templatemode.TemplateMode;
import org.togglz.spring.boot.actuate.thymeleaf.TogglzDialect;

/**
 * Processor for the <code>inactive</code> attribute in {@link TogglzDialect}. It shows or hides the given DOM container
 * based on feature state: <br>
 * <br>
 *
 * <pre>
 * &lt;span togglz:inactive="FEATURE"&gt;
 *    Only visible when FEATURE is inactive.
 * &lt;/span&gt;
 * </pre>
 *
 * Features may also be specified dynamically by using expressions:<br>
 * <br>
 *
 * <pre>
 * &lt;span togglz:inactive="${feature}"&gt;
 *    Only visible when feature resolved by evaluating ${feature} is inactive.
 * &lt;/span&gt;
 * </pre>
 *
 * When using Thymeleaf 2.0.x, literal feature names have to be quoted with single quotes. Thymeleaf from 2.1 onward
 * supports unquoted string literals.
 *
 * @author Hendrik Heneke
 * @since 1.0.1
 */
public class FeatureInactiveAttrProcessor extends AbstractFeatureAttrProcessor {

    public FeatureInactiveAttrProcessor(final TemplateMode templateMode, String dialectPrefix) {
        super(templateMode, dialectPrefix, "inactive", StandardIfTagProcessor.PRECEDENCE);
    }

    @Override
    protected boolean isVisible(final ITemplateContext context, final IProcessableElementTag tag, final AttributeName attributeName, final String attributeValue) {
        return !determineFeatureState(context, attributeValue, true);
    }
}

