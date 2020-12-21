package org.togglz.zookeeper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.client.ZKClientConfig;
import org.apache.zookeeper.client.ZooKeeperSaslClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.activation.UsernameActivationStrategy;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.FeatureStateStorageWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ryan Gardner
 */
public class ZookeeperStateRepositoryTest {

    public static final String TEST_ZNODE = "/test/features";
    private ZookeeperStateRepository stateRepository;

    static class ServerClientPair {
        public final TestingServer server;
        public final CuratorFramework client;

        ServerClientPair(TestingServer server, CuratorFramework client) {
            this.server = server;
            this.client = client;
        }
    }

    public static ServerClientPair startServer(Map<String, String> data) throws Exception {

        TestingServer server = new TestingServer();
        CuratorFramework client;
        // for environments where sasl is set, null it our for our test
        System.setProperty(ZKClientConfig.ENABLE_CLIENT_SASL_DEFAULT, "false");

        client = CuratorFrameworkFactory.builder().connectString(server.getConnectString()).retryPolicy(new RetryOneTime(2000)).build();
        client.start();

        for (Map.Entry<String, String> initialData : data.entrySet()) {
            client.create().creatingParentContainersIfNeeded().forPath(initialData.getKey(), initialData.getValue().getBytes(UTF_8));
        }
        return new ServerClientPair(server, client);
    }

    public static void stopServer(ServerClientPair pair) throws IOException {
        pair.client.close();
        pair.server.stop();
        System.clearProperty(ZooKeeperSaslClient.ENABLE_CLIENT_SASL_KEY);
    }

    ServerClientPair serverClientPair;

    public void setupTestWithEmptyDatastore() throws Exception {
        setupTestWithData(Collections.EMPTY_MAP);
    }

    public void setupTestWithData(Map<String, String> initialParameters) throws Exception {
        serverClientPair = startServer(initialParameters);
        stateRepository = ZookeeperStateRepository.newBuilder(serverClientPair.client, TEST_ZNODE).build();
    }

    @AfterEach
    public void cleanUp() throws Exception {
        stopServer(serverClientPair);
    }

    @Test
    public void testFeatureSavingAndLoading() throws Exception {
        setupTestWithEmptyDatastore();
        assertNull(stateRepository.getFeatureState(TestFeature.FEATURE));
        stateRepository.setFeatureState(new FeatureState(TestFeature.FEATURE));

        assertNotNull(stateRepository.getFeatureState(TestFeature.FEATURE));
    }

    @Test
    public void testActivationStrategySavingAndLoading() throws Exception {
        setupTestWithEmptyDatastore();
        FeatureState savedFeatureState = new FeatureState(TestFeature.FEATURE);
        savedFeatureState.setStrategyId(UsernameActivationStrategy.ID);
        savedFeatureState.setParameter(UsernameActivationStrategy.PARAM_USERS, "user1, user2, user3");
        stateRepository.setFeatureState(savedFeatureState);

        FeatureState loadedFeatureState = stateRepository.getFeatureState(TestFeature.FEATURE);

        assertTrue(reflectionEquals(savedFeatureState, loadedFeatureState));
    }

    @Test
    public void testEnabledStateSavingAndLoading() throws Exception {
        setupTestWithEmptyDatastore();
        FeatureState savedFeatureState = new FeatureState(TestFeature.FEATURE).enable();
        stateRepository.setFeatureState(savedFeatureState);

        FeatureState loadedFeatureState = stateRepository.getFeatureState(TestFeature.FEATURE);
        assertTrue(loadedFeatureState.isEnabled());

        stateRepository.setFeatureState(savedFeatureState.disable());
        loadedFeatureState = stateRepository.getFeatureState(TestFeature.FEATURE);
        assertFalse(loadedFeatureState.isEnabled());
    }

    @Test
    public void testZkNodeChangesUpdateFeatureState() throws Exception {
        setupTestWithEmptyDatastore();
        FeatureState savedFeatureState = new FeatureState(TestFeature.FEATURE);
        savedFeatureState.setStrategyId(UsernameActivationStrategy.ID);
        savedFeatureState.setParameter(UsernameActivationStrategy.PARAM_USERS, "user1, user2, user3");
        stateRepository.setFeatureState(savedFeatureState);

        FeatureState loadedFeatureState = stateRepository.getFeatureState(TestFeature.FEATURE);
        assertTrue(reflectionEquals(savedFeatureState, loadedFeatureState));

        // Modify data in ZK
        FeatureStateStorageWrapper externallySetStateWrapper = new FeatureStateStorageWrapper();
        FeatureState externallySetState = new FeatureState(TestFeature.FEATURE);
        ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(externallySetStateWrapper);

        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                serverClientPair.client.setData().forPath(TEST_ZNODE + "/FEATURE", json.getBytes(UTF_8));
                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        latch.await(2, TimeUnit.SECONDS);
        Thread.sleep(500);

        loadedFeatureState = stateRepository.getFeatureState(TestFeature.FEATURE);
        assertTrue(reflectionEquals(externallySetState, loadedFeatureState));
    }

    @Test
    public void testLoadingWithSavedState() throws Exception {
        // re-setup the server
        Map<String, String> initialData = new HashMap<>();
        initialData.put(TEST_ZNODE + "/FEATURE", "{\"enabled\":true,\"strategyId\":null,\"parameters\":{}}");
        // recreate the zookeeper server with the data we want for this test
        setupTestWithData(initialData);

        FeatureState expectedFeatureState = new FeatureState(TestFeature.FEATURE);
        expectedFeatureState.setEnabled(true);

        FeatureState loadedFeatureState = stateRepository.getFeatureState(TestFeature.FEATURE);
        assertTrue(reflectionEquals(expectedFeatureState, loadedFeatureState));
    }

    private enum TestFeature implements Feature {
        FEATURE,
    }
}
