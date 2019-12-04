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
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.standard.processor.AbstractStandardConditionalVisibilityTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.LazyResolvingFeatureManager;
import org.togglz.core.util.NamedFeature;

public abstract class AbstractFeatureAttrProcessor extends AbstractStandardConditionalVisibilityTagProcessor {

    private final FeatureManager featureManager;

    protected AbstractFeatureAttrProcessor(final TemplateMode templateMode, final String dialectPrefix, final String attributeName, final int precedence) {
        super(templateMode, dialectPrefix, attributeName, precedence);
        this.featureManager = new LazyResolvingFeatureManager();
    }

    /**
     * Determines the feature state
     *
     * @param context        the template context
     * @param attributeValue the attribute value
     * @param defaultState   the default state if the expression evaluates to null
     * @return the feature state
     */
    boolean determineFeatureState(final ITemplateContext context, final String attributeValue, boolean defaultState) {
        final IStandardExpressionParser expressionParser = StandardExpressions.getExpressionParser(context.getConfiguration());
        final IStandardExpression expression = expressionParser.parseExpression(context, attributeValue);
        final Object value = expression.execute(context);
        if (value != null) {
            return isFeatureActive(value.toString());
        }
        else {
            return defaultState;
        }
    }

    private boolean isFeatureActive(String name) {
        return featureManager.isActive(new NamedFeature(name));
    }

}
