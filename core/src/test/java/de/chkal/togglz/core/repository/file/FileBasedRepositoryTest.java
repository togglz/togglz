package de.chkal.togglz.core.repository.file;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.manager.FeatureState;
import de.chkal.togglz.core.repository.file.FileBasedRepository;

public class FileBasedRepositoryTest {

    private File file;

    @Before
    public void before() throws IOException {

        Properties p = new Properties();
        p.setProperty("FEATURE1", "true");
        p.setProperty("FEATURE1.users", "chkal,tester");
        p.setProperty("FEATURE2", "false");
        p.setProperty("FEATURE3", "true");

        file = File.createTempFile("test-file-repository", null);
        FileOutputStream fos = new FileOutputStream(file);
        p.store(fos, null);
        fos.close();

    }

    @After
    public void after() throws IOException {
        file.delete();
    }

    @Test
    public void testGetFeatureState() {

        FileBasedRepository repo = new FileBasedRepository(file);

        FeatureState state1 = repo.getFeatureState(MyFeature.FEATURE1);
        assertEquals(true, state1.isEnabled());
        assertEquals(Arrays.asList("chkal", "tester"), state1.getUsers());

        FeatureState state2 = repo.getFeatureState(MyFeature.FEATURE2);
        assertEquals(false, state2.isEnabled());
        assertEquals(Collections.emptyList(), state2.getUsers());

        FeatureState state3 = repo.getFeatureState(MyFeature.FEATURE3);
        assertEquals(true, state3.isEnabled());
        assertEquals(Collections.emptyList(), state3.getUsers());

    }

    @Test
    public void testSetFeatureState() throws IOException {

        // modify FEATURE1 and FEATURE2, don't touch FEATURE3
        FileBasedRepository repo = new FileBasedRepository(file);
        repo.setFeatureState(new FeatureState(MyFeature.FEATURE1, false));
        repo.setFeatureState(new FeatureState(MyFeature.FEATURE2, true, Arrays.asList("a", "b", "c")));

        FileInputStream fis = new FileInputStream(file);
        Properties p = new Properties();
        p.load(fis);

        assertEquals(4, p.size());
        assertEquals("false", p.getProperty("FEATURE1"));
        assertEquals("true", p.getProperty("FEATURE2"));
        assertEquals("a,b,c", p.getProperty("FEATURE2.users"));
        assertEquals("true", p.getProperty("FEATURE3"));

    }

    private static enum MyFeature implements Feature {

        FEATURE1, FEATURE2, FEATURE3;

        public boolean isEnabled() {
            return false;
        }

    }

}
