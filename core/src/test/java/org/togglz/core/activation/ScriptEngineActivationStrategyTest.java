package org.togglz.core.activation;

import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.SimpleFeatureUser;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptEngineActivationStrategyTest {

    private static final String JAVASCRIPT = "ECMAScript";
    private static final String UNKNOWN_LANGUAGE = "some language that doesn't exist";
    private static final String SOME_SCRIPT = "some content";
    private static final String INVALID_JAVASCRIPT = " = ,;";

    @Test
    void shouldReturnFalseForUnsupportedLanguage() {
        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        FeatureState state = aScriptState(UNKNOWN_LANGUAGE, SOME_SCRIPT);
        boolean active = strategy.isActive(state, aFeatureUser("john"));

        assertFalse(active);
    }

    @Test
    void shouldReturnFalseForInvalidJavaScript() {
        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        FeatureState state = aScriptState(JAVASCRIPT, INVALID_JAVASCRIPT);
        boolean active = strategy.isActive(state, aFeatureUser("john"));

        assertFalse(active);
    }

    @Test
    void shouldReturnSameResultAsScriptForLiterals() {
        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        FeatureState stateAlwaysTrue = aScriptState(JAVASCRIPT, "1 == 1");
        assertTrue(strategy.isActive(stateAlwaysTrue, aFeatureUser("john")));

        FeatureState stateAlwaysFalse = aScriptState(JAVASCRIPT, "0 == 1");
        assertFalse(strategy.isActive(stateAlwaysFalse, aFeatureUser("john")));
    }

    @Test
    void scriptCanAccessCurrentUser() {
        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        FeatureState state = aScriptState(JAVASCRIPT, "user.name == 'john'");

        assertTrue(strategy.isActive(state, aFeatureUser("john")));
        assertFalse(strategy.isActive(state, aFeatureUser("jim")));
    }

    @Test
    void scriptCanAccessUserAttributes() {
        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        FeatureState ageCheck = aScriptState(JAVASCRIPT, "user.getAttribute('age') >= 21");

        SimpleFeatureUser child = aFeatureUser("john");
        child.setAttribute("age", 12);
        assertFalse(strategy.isActive(ageCheck, child));

        SimpleFeatureUser adult = aFeatureUser("peter");
        adult.setAttribute("age", 25);
        assertTrue(strategy.isActive(ageCheck, adult));
    }

    @Test
    void scriptCanAccessCurrentDate() {
        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        // date.getYear() is a two-digit year
        int currentYear = Calendar.getInstance().get(GregorianCalendar.YEAR) - 1900;

        FeatureState trueForCurrentYear = aScriptState(JAVASCRIPT, "date.year == " + currentYear);
        assertTrue(strategy.isActive(trueForCurrentYear, aFeatureUser("john")));

        FeatureState trueForNextYear = aScriptState(JAVASCRIPT, "date.year > " + currentYear);
        assertFalse(strategy.isActive(trueForNextYear, aFeatureUser("john")));
    }

    @Test
    void shouldSupportMultilineScripts() {
        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        FeatureState state = aScriptState(JAVASCRIPT,
            "var len = user.name.length();\r\n len % 2 == 0;\n");

        assertTrue(strategy.isActive(state, aFeatureUser("john")));
        assertFalse(strategy.isActive(state, aFeatureUser("jim")));
    }

    @Test
    void shouldSupportScriptWithFunction() {
        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        FeatureState state = aScriptState(JAVASCRIPT,
            "function isJohn(name) { return name == 'john' }; isJohn(user.name);");

        assertTrue(strategy.isActive(state, aFeatureUser("john")));
        assertFalse(strategy.isActive(state, aFeatureUser("jim")));
    }

    @Test
    void shouldReturnScript() {
        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();
        assertEquals("script", strategy.getId());
    }

    @Test
    void shouldReturnStrategyName() {
        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        assertEquals("Java Scripting API", strategy.getName());
    }

    @Test
    void shouldContainDescription() {
        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        assertEquals("The script language to use. Your system seems to support the following languages: ECMAScript, ECMAScript", strategy.getParameters()[0].getDescription());
        assertEquals("The script to check if the feature is active. The script context provides access to some default objects. The variable 'user' refers to the current acting FeatureUser and 'date' to the current time represented as a java.util.Date.", strategy.getParameters()[1].getDescription());
    }

    @Test
    void shouldContainLang() {
        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        assertEquals("lang", strategy.getParameters()[0].getName());
    }

    @Test
    void shouldContainLanguage() {
        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        assertEquals("Language", strategy.getParameters()[0].getLabel());
    }

    @Test
    void shouldNotBeLargeText() {
        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        assertFalse(strategy.getParameters()[0].isLargeText());
    }

    @Test
    void shouldNotBeOptional() {
        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        assertFalse(strategy.getParameters()[0].isOptional());
    }

    @Test
    void shouldNotBeValidParameter() {
        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        assertFalse(strategy.getParameters()[0].isValid("notValid"));
    }

    @Test
    void shouldBeValidParameter() {
        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        assertTrue(strategy.getParameters()[0].isValid("ECMAScript"));
    }

    private FeatureState aScriptState(String lang, String script) {
        return new FeatureState(ScriptFeature.FEATURE)
            .setStrategyId(ScriptEngineActivationStrategy.ID)
            .setParameter(ScriptEngineActivationStrategy.PARAM_LANG, lang)
            .setParameter(ScriptEngineActivationStrategy.PARAM_SCRIPT, script);
    }

    private SimpleFeatureUser aFeatureUser(String string) {
        return new SimpleFeatureUser(string);
    }

    private enum ScriptFeature implements Feature {
        FEATURE
    }
}
