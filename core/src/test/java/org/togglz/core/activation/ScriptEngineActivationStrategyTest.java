package org.togglz.core.activation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.SimpleFeatureUser;

public class ScriptEngineActivationStrategyTest {

    private static final String JAVASCRIPT = "ECMAScript";
    private static final String UNKNOWN_LANGUAGE = "some language that doesn't exist";
    private static final String SOME_SCRIPT = "some content";
    private static final String INVALID_JAVASCRIPT = " = ,;";

    @Test
    public void shouldReturnFalseForUnsupportedLanguage() {

        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        FeatureState state = aScriptState(UNKNOWN_LANGUAGE, SOME_SCRIPT);
        boolean active = strategy.isActive(state, aFeatureUser("john"));

        assertThat(active).isFalse();

    }

    @Test
    public void shouldReturnFalseForInvalidJavaScript() {

        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        FeatureState state = aScriptState(JAVASCRIPT, INVALID_JAVASCRIPT);
        boolean active = strategy.isActive(state, aFeatureUser("john"));

        assertThat(active).isFalse();

    }

    @Test
    public void shouldReturnSameResultAsScriptForLiterals() {

        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        FeatureState stateAlwaysTrue = aScriptState(JAVASCRIPT, "1 == 1");
        assertThat(strategy.isActive(stateAlwaysTrue, aFeatureUser("john"))).isTrue();

        FeatureState stateAlwaysFalse = aScriptState(JAVASCRIPT, "0 == 1");
        assertThat(strategy.isActive(stateAlwaysFalse, aFeatureUser("john"))).isFalse();

    }

    @Test
    public void scriptCanAccessCurrentUser() {

        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        FeatureState state = aScriptState(JAVASCRIPT, "user.name == 'john'");

        assertThat(strategy.isActive(state, aFeatureUser("john"))).isTrue();
        assertThat(strategy.isActive(state, aFeatureUser("jim"))).isFalse();

    }

    @Test
    public void scriptCanAccessUserAttributes() {

        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        FeatureState ageCheck = aScriptState(JAVASCRIPT, "user.getAttribute('age') >= 21");

        SimpleFeatureUser child = aFeatureUser("john");
        child.setAttribute("age", 12);
        assertThat(strategy.isActive(ageCheck, child)).isFalse();

        SimpleFeatureUser adult = aFeatureUser("peter");
        adult.setAttribute("age", 25);
        assertThat(strategy.isActive(ageCheck, adult)).isTrue();

    }

    @Test
    public void scriptCanAccessCurrentDate() {

        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        // date.getYear() is a two-digit year
        int currentYear = Calendar.getInstance().get(GregorianCalendar.YEAR) - 1900;

        FeatureState trueForCurrentYear = aScriptState(JAVASCRIPT, "date.year == " + currentYear);
        assertThat(strategy.isActive(trueForCurrentYear, aFeatureUser("john"))).isTrue();

        FeatureState trueForNextYear = aScriptState(JAVASCRIPT, "date.year > " + currentYear);
        assertThat(strategy.isActive(trueForNextYear, aFeatureUser("john"))).isFalse();

    }

    @Test
    public void shouldSupportMultilineScripts() {

        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        FeatureState state = aScriptState(JAVASCRIPT,
            "var len = user.name.length();\r\n len % 2 == 0;\n");

        assertThat(strategy.isActive(state, aFeatureUser("john"))).isTrue();
        assertThat(strategy.isActive(state, aFeatureUser("jim"))).isFalse();

    }

    @Test
    public void shouldSupportScriptWithFunction() {

        ScriptEngineActivationStrategy strategy = new ScriptEngineActivationStrategy();

        FeatureState state = aScriptState(JAVASCRIPT,
            "function isJohn(name) { return name == 'john' }; isJohn(user.name);");

        assertThat(strategy.isActive(state, aFeatureUser("john"))).isTrue();
        assertThat(strategy.isActive(state, aFeatureUser("jim"))).isFalse();

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
        FEATURE;
    }

}
