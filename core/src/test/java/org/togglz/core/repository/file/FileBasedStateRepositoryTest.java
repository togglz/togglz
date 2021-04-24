package org.togglz.core.repository.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.activation.UsernameActivationStrategy;
import org.togglz.core.repository.FeatureState;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

            assertEquals(5, newProps.size());

            // FEATURE1: disabled without any strategy or properties
            assertEquals("false", newProps.getProperty("FEATURE1"));

            // FEATURE2: enabled with a strategy and one property
            assertEquals("true", newProps.getProperty("FEATURE2"));
            assertEquals("some-strategy", newProps.getProperty("FEATURE2.strategy"));
            assertEquals("myvalue", newProps.getProperty("FEATURE2.param.myparam"));

            // FEATURE3: didn't change
            assertEquals("true", newProps.getProperty("FEATURE3"));

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

            assertEquals(4, newProps.size());
            assertEquals("true", newProps.getProperty("FEATURE1"));
            assertEquals("my-strategy", newProps.getProperty("FEATURE1.strategy"));
            assertEquals("some-value", newProps.getProperty("FEATURE1.param.myparam"));
            assertEquals("something-else", newProps.getProperty("FEATURE1.param.other"));
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

            assertEquals(2, newProps.size());
            assertEquals("true", newProps.getProperty("FEATURE1"));
            assertEquals("my-strategy", newProps.getProperty("FEATURE1.strategy"));
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

            assertEquals(2, newProps.size());
            assertEquals("true", newProps.getProperty("FEATURE1"));
            assertEquals("something", newProps.getProperty("FEATURE1.strategy"));
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

            assertEquals(1, newProps.size());
            assertEquals("true", newProps.getProperty("FEATURE1"));
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

            assertEquals(3, newProps.size());
            assertEquals("true", newProps.getProperty("FEATURE1"));
            assertEquals("something", newProps.getProperty("FEATURE1.strategy"));
            assertEquals("bar", newProps.getProperty("FEATURE1.param.foo"));
        } finally {
            file.delete();
        }
    }

    @Test
    public void createMissingFile() throws IOException {
        File file = new File("test.properties");

        try {
            FileBasedStateRepository repo = new FileBasedStateRepository(file);

            repo.setFeatureState(new FeatureState(MyFeature.FEATURE1, true));

            Properties newProps = readPropertiesFile(file);

            assertEquals(1, newProps.size());
            assertEquals("true", newProps.getProperty("FEATURE1"));
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

            assertEquals(3, newProps.size());
            assertEquals("false",newProps.getProperty("FEATURE1"));
            assertEquals("something",newProps.getProperty("FEATURE1.strategy"));
            assertEquals("bar",newProps.getProperty("FEATURE1.param.foo"));
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
            assertTrue(state1.isEnabled());
            assertEquals(UsernameActivationStrategy.ID, state1.getStrategyId());
            assertEquals(1, state1.getParameterNames().size());
            assertEquals("chkal,tester", state1.getParameter(UsernameActivationStrategy.PARAM_USERS));

            // FEATURE2: disabled, no strategy, no parameters
            FeatureState state2 = repo.getFeatureState(MyFeature.FEATURE2);
            assertFalse(state2.isEnabled());
            assertNull(state2.getStrategyId());
            assertEquals(0, state2.getParameterNames().size());

            // FEATURE3: enabled, no strategy, no parameters
            FeatureState state3 = repo.getFeatureState(MyFeature.FEATURE3);
            assertTrue(state3.isEnabled());
            assertNull(state2.getStrategyId());
            assertEquals(0, state3.getParameterNames().size());

            FeatureState state4 = repo.getFeatureState(MyFeature.FEATURE4);
            assertNull(state4);

            /*
             * Now change one feature and check the new format is persisted
             */

            state1.disable();
            repo.setFeatureState(state1);

            Properties newProps = readPropertiesFile(file);
            assertEquals("false", newProps.getProperty("FEATURE1"));
            assertEquals(UsernameActivationStrategy.ID, newProps.getProperty("FEATURE1.strategy"));
            assertEquals("chkal,tester", newProps.getProperty("FEATURE1.param.users"));
            assertNull(newProps.getProperty("FEATURE1.users"));
        } finally {
            file.delete();
        }
    }

    private static Properties readPropertiesFile(File file) throws IOException {
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

    private enum MyFeature implements Feature {
        FEATURE1,
        FEATURE2,
        FEATURE3,
        FEATURE4
    }
}
