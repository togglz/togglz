package org.togglz.core.repository.url;

import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.activation.URLActivationStrategy;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.FeatureState;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: Eli Abramovitch
 * Date: 3/12/13
 * Time: 10:08 AM
 *
 */
public class URLStateRepositoryTest {

    @Test
    public void updateMultipleStates() throws IOException {

        URLStateRepository repo = URLStateRepository.getInstance();
        repo.setFeatureState(MyFeature.FEATURE1 + ",true");
        repo.setFeatureState(MyFeature.FEATURE2 + ",false,user1");
        repo.setFeatureState(MyFeature.FEATURE3 + ",true");

        // modify FEATURE1 and FEATURE2, don't touch FEATURE3
        repo.setFeatureState(MyFeature.FEATURE1 + "," + URLStateRepository.DISABLE_FEATURE_TOKEN);
        repo.setFeatureState(MyFeature.FEATURE2 + "," + URLStateRepository.ENABLE_FEATURE_TOKEN);

        assertThat(repo.size(), is(3));

        // FEATURE1: disabled without any strategy or properties
        assertThat(repo.getFeatureState(MyFeature.FEATURE1).isEnabled(), is(false));
        assertThat(repo.getFeatureState(MyFeature.FEATURE1).getFeature().isActive(), is(true));
        assertThat(repo.isActive(MyFeature.FEATURE1), is(false));

        // FEATURE2: enabled with a strategy and one property
        FeatureState feature2 = repo.getFeatureState(MyFeature.FEATURE2);
        assertThat(feature2.isEnabled(), is(true));
        assertThat(feature2.getStrategyId(), is(URLActivationStrategy.ID));
        assertThat(feature2.getParameter(URLActivationStrategy.PARAM_USERS), is("user1"));
        assertThat(repo.getFeatureState(MyFeature.FEATURE2).getFeature().isActive(), is(false));
        assertThat(repo.getFeatureState(MyFeature.FEATURE2).getParameter(URLActivationStrategy.PARAM_USERS).split(",").length, is(1));
        assertThat(repo.isActive(MyFeature.FEATURE2), is(false));

        // FEATURE3: didn't change
        assertThat(repo.getFeatureState(MyFeature.FEATURE3).isEnabled(), is(true));
        assertThat(repo.getFeatureState(MyFeature.FEATURE3).getParameter(URLActivationStrategy.PARAM_USERS), nullValue());
        assertThat(repo.getFeatureState(MyFeature.FEATURE3).getFeature().isActive(), is(true));

        // add properties FEATURE3
        repo.setFeatureState("FEATURE3,true,user1,user2");
        assertThat(repo.getFeatureState(MyFeature.FEATURE3).getParameter(URLActivationStrategy.PARAM_USERS).split(",").length, is(2));
        assertThat(repo.getFeatureState(MyFeature.FEATURE3).getFeature().isActive(), is(true));

        repo.setFeatureState(URLStateRepository.CLEAR_FEATURE_TOKEN);
        assertThat(repo.size(), is(0));
    }

    private static enum MyFeature implements Feature {

        FEATURE1,
        FEATURE2,
        FEATURE3,
        FEATURE4;

        @Override
        public boolean isActive() {
            return FeatureContext.getFeatureManager().isActive(this);
        }

    }
}
