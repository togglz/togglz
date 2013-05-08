package org.togglz.core.repository.file;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.activation.UsernameActivationStrategy;
import org.togglz.core.repository.FeatureState;

public class FileBasedStateRepositoryTest {

    @Test
    public void updateMultipleStates() throws IOException {

        Properties initialProps = new Properties();
        initialProps.setProperty("FEATURE1", "true");
        initialProps.setProperty("FEATURE1.strategy", UsernameActivationStrategy.ID);
        initialProps.setProperty("FEATURE1.param.users", "chkal,tester");
        initialProps.setProperty("FEATURE2", "false");
        initialProps.setProperty("FEATURE3", "true");

        File file = createPropertiesFile(initialProps);

        try {

            // modify FEATURE1 and FEATURE2, don't touch FEATURE3
            FileBasedStateRepository repo = new FileBasedStateRepository(file);
            repo.setFeatureState(new FeatureState(MyFeature.FEATURE1, false));
            repo.setFeatureState(new FeatureState(MyFeature.FEATURE2, true)
                .setStrategyId("some-strategy").setParameter("myparam", "myvalue"));

            Properties newProps = readPropertiesFile(file);

            assertThat(newProps.size(), is(5));

            // FEATURE1: disabled without any strategy or properties
            assertThat(newProps.getProperty("FEATURE1"), is("false"));

            // FEATURE2: enabled with a strategy and one property
            assertThat(newProps.getProperty("FEATURE2"), is("true"));
            assertThat(newProps.getProperty("FEATURE2.strategy"), is("some-strategy"));
            assertThat(newProps.getProperty("FEATURE2.param.myparam"), is("myvalue"));

            // FEATURE3: didn't change
            assertThat(newProps.getProperty("FEATURE3"), is("true"));

        } finally {
            file.delete();
        }

    }

    @Test
    public void addNewProperty() throws IOException {

        Properties initialProps = new Properties();
        initialProps.setProperty("FEATURE1", "true");
        initialProps.setProperty("FEATURE1.strategy", "my-strategy");
        initialProps.setProperty("FEATURE1.param.myparam", "some-value");

        File file = createPropertiesFile(initialProps);

        try {

            FileBasedStateRepository repo = new FileBasedStateRepository(file);

            FeatureState state = repo.getFeatureState(MyFeature.FEATURE1);
            state.setParameter("other", "something-else");
            repo.setFeatureState(state);

            Properties newProps = readPropertiesFile(file);

            assertThat(newProps.size(), is(4));
            assertThat(newProps.getProperty("FEATURE1"), is("true"));
            assertThat(newProps.getProperty("FEATURE1.strategy"), is("my-strategy"));
            assertThat(newProps.getProperty("FEATURE1.param.myparam"), is("some-value"));
            assertThat(newProps.getProperty("FEATURE1.param.other"), is("something-else"));

        } finally {
            file.delete();
        }

    }

    @Test
    public void removeProperty() throws IOException {

        Properties initialProps = new Properties();
        initialProps.setProperty("FEATURE1", "true");
        initialProps.setProperty("FEATURE1.strategy", "my-strategy");
        initialProps.setProperty("FEATURE1.param.myparam", "some-value");

        File file = createPropertiesFile(initialProps);

        try {

            FileBasedStateRepository repo = new FileBasedStateRepository(file);

            FeatureState state = repo.getFeatureState(MyFeature.FEATURE1);
            state.setParameter("myparam", null);
            repo.setFeatureState(state);

            Properties newProps = readPropertiesFile(file);

            assertThat(newProps.size(), is(2));
            assertThat(newProps.getProperty("FEATURE1"), is("true"));
            assertThat(newProps.getProperty("FEATURE1.strategy"), is("my-strategy"));

        } finally {
            file.delete();
        }

    }

    @Test
    public void setStrategyId() throws IOException {

        Properties initialProps = new Properties();
        initialProps.setProperty("FEATURE1", "true");

        File file = createPropertiesFile(initialProps);

        try {

            FileBasedStateRepository repo = new FileBasedStateRepository(file);

            FeatureState state = repo.getFeatureState(MyFeature.FEATURE1);
            state.setStrategyId("something");
            repo.setFeatureState(state);

            Properties newProps = readPropertiesFile(file);

            assertThat(newProps.size(), is(2));
            assertThat(newProps.getProperty("FEATURE1"), is("true"));
            assertThat(newProps.getProperty("FEATURE1.strategy"), is("something"));

        } finally {
            file.delete();
        }

    }

    @Test
    public void removeStrategy() throws IOException {

        Properties initialProps = new Properties();
        initialProps.setProperty("FEATURE1", "true");
        initialProps.setProperty("FEATURE1.strategy", "foo");

        File file = createPropertiesFile(initialProps);

        try {

            FileBasedStateRepository repo = new FileBasedStateRepository(file);

            FeatureState state = repo.getFeatureState(MyFeature.FEATURE1);
            state.setStrategyId(null);
            repo.setFeatureState(state);

            Properties newProps = readPropertiesFile(file);

            assertThat(newProps.size(), is(1));
            assertThat(newProps.getProperty("FEATURE1"), is("true"));

        } finally {
            file.delete();
        }

    }

    @Test
    public void enableFeature() throws IOException {

        Properties initialProps = new Properties();
        initialProps.setProperty("FEATURE1", "false");
        initialProps.setProperty("FEATURE1.strategy", "something");
        initialProps.setProperty("FEATURE1.param.foo", "bar");

        File file = createPropertiesFile(initialProps);

        try {

            FileBasedStateRepository repo = new FileBasedStateRepository(file);

            FeatureState state = repo.getFeatureState(MyFeature.FEATURE1);
            state.enable();
            repo.setFeatureState(state);

            Properties newProps = readPropertiesFile(file);

            assertThat(newProps.size(), is(3));
            assertThat(newProps.getProperty("FEATURE1"), is("true"));
            assertThat(newProps.getProperty("FEATURE1.strategy"), is("something"));
            assertThat(newProps.getProperty("FEATURE1.param.foo"), is("bar"));

        } finally {
            file.delete();
        }

    }

    @Test
    public void disableFeature() throws IOException {

        Properties initialProps = new Properties();
        initialProps.setProperty("FEATURE1", "true");
        initialProps.setProperty("FEATURE1.strategy", "something");
        initialProps.setProperty("FEATURE1.param.foo", "bar");

        File file = createPropertiesFile(initialProps);

        try {

            FileBasedStateRepository repo = new FileBasedStateRepository(file);

            FeatureState state = repo.getFeatureState(MyFeature.FEATURE1);
            state.disable();
            repo.setFeatureState(state);

            Properties newProps = readPropertiesFile(file);

            assertThat(newProps.size(), is(3));
            assertThat(newProps.getProperty("FEATURE1"), is("false"));
            assertThat(newProps.getProperty("FEATURE1.strategy"), is("something"));
            assertThat(newProps.getProperty("FEATURE1.param.foo"), is("bar"));

        } finally {
            file.delete();
        }

    }

    @Test
    public void readFeatureStateFromOldFormat() throws IOException {

        Properties initialProps = new Properties();
        initialProps.setProperty("FEATURE1", "true");
        initialProps.setProperty("FEATURE1.users", "chkal,tester");
        initialProps.setProperty("FEATURE2", "false");
        initialProps.setProperty("FEATURE3", "true");

        File file = createPropertiesFile(initialProps);

        try {

            /*
             * Check the state of the repository
             */

            FileBasedStateRepository repo = new FileBasedStateRepository(file);

            // FEATURE1: enabled, strategy set by migration code, one property containing user list
            FeatureState state1 = repo.getFeatureState(MyFeature.FEATURE1);
            assertEquals(true, state1.isEnabled());
            assertEquals(UsernameActivationStrategy.ID, state1.getStrategyId());
            assertEquals(1, state1.getParameterNames().size());
            assertEquals("chkal,tester", state1.getParameter(UsernameActivationStrategy.PARAM_USERS));

            // FEATURE2: disabled, no strategy, no parameters
            FeatureState state2 = repo.getFeatureState(MyFeature.FEATURE2);
            assertEquals(false, state2.isEnabled());
            assertEquals(null, state2.getStrategyId());
            assertEquals(0, state2.getParameterNames().size());

            // FEATURE3: enabled, no strategy, no parameters
            FeatureState state3 = repo.getFeatureState(MyFeature.FEATURE3);
            assertEquals(true, state3.isEnabled());
            assertEquals(null, state2.getStrategyId());
            assertEquals(0, state3.getParameterNames().size());

            FeatureState state4 = repo.getFeatureState(MyFeature.FEATURE4);
            assertNull(state4);

            /*
             * Now change one feature and check the new format is persisted
             */

            state1.disable();
            repo.setFeatureState(state1);

            Properties newProps = readPropertiesFile(file);
            assertThat(newProps.getProperty("FEATURE1"), is("false"));
            assertThat(newProps.getProperty("FEATURE1.strategy"), is(UsernameActivationStrategy.ID));
            assertThat(newProps.getProperty("FEATURE1.param.users"), is("chkal,tester"));
            assertThat(newProps.getProperty("FEATURE1.users"), nullValue());

        } finally {
            file.delete();
        }

    }

    private static Properties readPropertiesFile(File file) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(file);
        Properties p = new Properties();
        p.load(fis);
        return p;
    }

    private static File createPropertiesFile(Properties p) throws IOException {
        File file = File.createTempFile("test-file-repository", null);
        FileOutputStream fos = new FileOutputStream(file);
        p.store(fos, null);
        fos.close();
        return file;
    }

    private static enum MyFeature implements Feature {
        FEATURE1,
        FEATURE2,
        FEATURE3,
        FEATURE4;
    }

}
