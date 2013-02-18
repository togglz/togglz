package org.togglz.core.activation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;

public class ScriptEngineActivationStrategy implements ActivationStrategy {

    private final Log log = LogFactory.getLog(ScriptEngineActivationStrategy.class);

    public static final String ID = "script";
    public static final String PARAM_SCRIPT = "script";
    public static final String PARAM_LANG = "lang";

    private final ScriptEngineManager engineManager;

    public ScriptEngineActivationStrategy() {
        engineManager = new ScriptEngineManager();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "ScriptEngine";
    }

    @Override
    public boolean isActive(FeatureState featureState, FeatureUser user) {

        String lang = featureState.getParameter(PARAM_LANG);
        String script = featureState.getParameter(PARAM_SCRIPT);

        ScriptEngine engine = engineManager.getEngineByName(lang);
        if (engine == null) {
            log.error("Could not find script engine for: " + lang);
            return false;
        }

        engine.put("user", user);
        engine.put("date", new Date());
        try {

            Object result = engine.eval(script);
            if (result instanceof Boolean) {
                return ((Boolean) result).booleanValue();
            }

        } catch (ScriptException e) {
            log.error("Could not evaluate script for feature " + featureState.getFeature().name() + ": " + e.getMessage());
        }
        return false;

    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
                ParameterBuilder.create(PARAM_LANG).label("Language")
                    .description("The script language to use. Your system seems to support the following languages: " +
                        Strings.join(getSupportedLanguages(), ", ")),
                ParameterBuilder.create(PARAM_SCRIPT).label("Script").largeText()
                    .description("The script to check if the feature is active. " +
                        "The script context provides access to some default objects. " +
                        "The variable 'user' refers to the current acting FeatureUser " +
                        "and 'date' to the current time represented as a java.util.Date.")
        };
    }

    private List<String> getSupportedLanguages() {
        List<String> languages = new ArrayList<String>();
        for (ScriptEngineFactory factory : engineManager.getEngineFactories()) {
            languages.add(factory.getLanguageName());
        }
        return languages;
    }

}
