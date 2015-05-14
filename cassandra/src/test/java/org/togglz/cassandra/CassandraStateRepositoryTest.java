package org.togglz.cassandra;

import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import org.cassandraunit.AbstractCassandraUnit4TestCase;
import org.cassandraunit.dataset.DataSet;
import org.cassandraunit.dataset.yaml.ClassPathYamlDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.activation.UsernameActivationStrategy;
import org.togglz.core.repository.FeatureState;

import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Test for {@link CassandraStateRepository}
 *
 * @author artur@callfire.com
 */
public class CassandraStateRepositoryTest extends AbstractCassandraUnit4TestCase {

    private CassandraStateRepository stateRepository;

    @Override
    public DataSet getDataSet() {
        return new ClassPathYamlDataSet("yaml/testDataSet.yaml");
    }

    @Before
    public void setupTest() throws Exception {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();

        Keyspace keyspace = new KeyspaceBuilder("Test Cluster", "TogglzTest").setThriftPort(9171).build();
        stateRepository = CassandraStateRepository.newBuilder(keyspace).build();
    }

    @After
    public void cleanUp() throws Exception {
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }

    @Test
    public void testFeatureSavingAndLoading() {
        assertNull(stateRepository.getFeatureState(TestFeature.FEATURE));
        stateRepository.setFeatureState(new FeatureState(TestFeature.FEATURE));

        assertNotNull(stateRepository.getFeatureState(TestFeature.FEATURE));
    }

    @Test
    public void testActivationStrategySavingAndLoading() {
        FeatureState savedFeatureState = new FeatureState(TestFeature.FEATURE);
        savedFeatureState.setStrategyId(UsernameActivationStrategy.ID);
        savedFeatureState.setParameter(UsernameActivationStrategy.PARAM_USERS, "user1, user2, user3");
        stateRepository.setFeatureState(savedFeatureState);

        FeatureState loadedFeatureState = stateRepository.getFeatureState(TestFeature.FEATURE);

        assertThat(reflectionEquals(savedFeatureState, loadedFeatureState), is(true));
    }

    @Test
    public void testEnabledStateSavingAndLoading() {
        FeatureState savedFeatureState = new FeatureState(TestFeature.FEATURE).enable();
        stateRepository.setFeatureState(savedFeatureState);

        FeatureState loadedFeatureState = stateRepository.getFeatureState(TestFeature.FEATURE);
        assertThat(loadedFeatureState.isEnabled(), is(true));

        stateRepository.setFeatureState(savedFeatureState.disable());
        loadedFeatureState = stateRepository.getFeatureState(TestFeature.FEATURE);
        assertThat(loadedFeatureState.isEnabled(), is(false));
    }

    @Test
    public void testAutomaticCreationOfColumnFamily() throws ConnectionException {
        String columnFamilyName = "I_dont_exist";

        Keyspace keyspace = new KeyspaceBuilder("Test Cluster", "TogglzTest").setThriftPort(9171).build();
        assertNull(keyspace.describeKeyspace().getColumnFamily(columnFamilyName));

        CassandraStateRepository.newBuilder(keyspace)
                .autoCreateColumnFamily(true)
                .columnFamily(columnFamilyName)
                .build();

        assertNotNull(keyspace.describeKeyspace().getColumnFamily(columnFamilyName));
    }

    private static enum TestFeature implements Feature {
        FEATURE,
    }
}