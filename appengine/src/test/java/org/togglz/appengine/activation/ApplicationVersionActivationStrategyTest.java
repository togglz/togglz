package org.togglz.appengine.activation;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.SimpleFeatureUser;

public class ApplicationVersionActivationStrategyTest {

    private static final String JOHN = "john";
    private final LocalServiceTestHelper helper = new LocalServiceTestHelper();
    private static final String CURRENT_VERSION = "beta.141242134213";

    @Before
    public void setup() {
        SystemProperty.applicationVersion.set(CURRENT_VERSION);
        helper.setUp();
    }

    @Test
    public void shouldReturnFalseForEmptyVersion() {
        ApplicationVersionActivationStrategy strategy = new ApplicationVersionActivationStrategy();
        FeatureState state = aVersionState("");
        boolean active = strategy.isActive(state, aFeatureUser(JOHN));
        Assert.assertFalse(active);
    }

    @Test
    public void shouldReturnFalseForNullVersion() {
        ApplicationVersionActivationStrategy strategy = new ApplicationVersionActivationStrategy();
        FeatureState state = aVersionState(null);
        boolean active = strategy.isActive(state, aFeatureUser(JOHN));
        Assert.assertFalse(active);
    }

    @Test
    public void shouldReturnFalseWhenCurrentVersionCannotBeResolved() {
        SystemProperty.applicationVersion.set("");
        ApplicationVersionActivationStrategy strategy = new ApplicationVersionActivationStrategy();
        FeatureState state = aVersionState(CURRENT_VERSION);
        boolean active = strategy.isActive(state, aFeatureUser(JOHN));
        Assert.assertFalse(active);
    }

    @Test
    public void shouldReturnTrueWhenMatchesServerCurrentVersion() {
        ApplicationVersionActivationStrategy strategy = new ApplicationVersionActivationStrategy();
        FeatureState state = aVersionState(CURRENT_VERSION);
        boolean active = strategy.isActive(state, aFeatureUser(JOHN));
        Assert.assertTrue(active);
    }

    @Test
    public void shouldReturnTrueWhenAtLeastOneOfTheVersionsMatchServerCurrentVersion() {
        ApplicationVersionActivationStrategy strategy = new ApplicationVersionActivationStrategy();
        FeatureState state = aVersionState(CURRENT_VERSION + ",alpha,dev,uat");
        boolean active = strategy.isActive(state, aFeatureUser(JOHN));
        Assert.assertTrue(active);
    }

    @Test
    public void shouldReturnFalseWhenNoneOfTheVersionsMatchTheServerCurrentVersion() {
        ApplicationVersionActivationStrategy strategy = new ApplicationVersionActivationStrategy();
        FeatureState state = aVersionState("test,alpha,dev,uat,rc1");
        boolean active = strategy.isActive(state, aFeatureUser(JOHN));
        Assert.assertFalse(active);
    }

    private FeatureState aVersionState(String lang) {
        return new FeatureState(ScriptFeature.FEATURE)
            .setStrategyId(ApplicationVersionActivationStrategy.ID)
            .setParameter(ApplicationVersionActivationStrategy.PARAM_VERSIONS, lang);
    }

    private SimpleFeatureUser aFeatureUser(String string) {
        return new SimpleFeatureUser(string);
    }

    private enum ScriptFeature implements Feature {
        FEATURE;
    }

}