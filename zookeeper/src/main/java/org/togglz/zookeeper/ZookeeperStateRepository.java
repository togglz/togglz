package org.togglz.zookeeper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.util.FeatureStateStorageWrapper;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.*;
import static org.togglz.core.util.FeatureStateStorageWrapper.featureStateForWrapper;

public class ZookeeperStateRepository implements StateRepository, TreeCacheListener {

    private static final Logger log = LoggerFactory.getLogger(ZookeeperStateRepository.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private TreeCache treeCache;
    private ConcurrentMap<String, FeatureStateStorageWrapper> states;

    protected final CuratorFramework curatorFramework;
    protected final String featuresZnode;

    // the initialization happens in another thread asynchronously.
    // rather than wait for
    protected CountDownLatch initializationLatch = new CountDownLatch(1);

    private ZookeeperStateRepository(Builder builder) throws Exception {
        this.curatorFramework = builder.curatorFramework;
        this.featuresZnode = builder.featuresZnode;
        initializeFeaturePath();
        initializeStateCache();
    }

    private void initializeFeaturePath() {
        try {
            curatorFramework.createContainers(featuresZnode);
        } catch (Exception e) {
            throw new RuntimeException("couldn't initialize the zookeeper state repository", e);
        }
    }

    private void initializeStateCache() throws Exception {
        states = new ConcurrentHashMap<>();
        // the treecache will keep a copy of the data in memory along with
        // setting up watchers for addition / removal of nodes
        treeCache = new TreeCache(curatorFramework, featuresZnode);
        treeCache.getListenable().addListener(this);
        treeCache.start();

        long startTime = System.nanoTime();
        log.info("Waiting for zookeeper state to be fully read");
        initializationLatch.await();
        long duration = System.nanoTime() - startTime;
        log.debug("Initialized the zookeeper state repository in {} ms", TimeUnit.NANOSECONDS.toMillis(duration));
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        FeatureStateStorageWrapper wrapper = states.get(feature.name());
        if (wrapper != null) {
            return featureStateForWrapper(feature, wrapper);
        }
        return null;
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        FeatureStateStorageWrapper wrapper = FeatureStateStorageWrapper.wrapperForFeatureState(featureState);
        try {
            String json = objectMapper.writeValueAsString(wrapper);
            String path = featuresZnode + "/" + featureState.getFeature().name();
            curatorFramework.createContainers(path);
            curatorFramework.setData().forPath(path, json.getBytes(UTF_8));
            states.put(featureState.getFeature().name(), wrapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent event) throws Exception {
        String featureName;
        ChildData eventData = event.getData();
        switch (event.getType()) {
            case NODE_ADDED:
                String addedPath = eventData.getPath();
                if (pathHasAFeatureInIt(addedPath)) {
                    featureName = getFeatureNameFromPath(addedPath);

                    if (featureName.contains("/")) {
                        break;
                    }
                    if (eventData.getData().length > 0) {
                        FeatureStateStorageWrapper featureState = objectMapper.readValue(eventData.getData(), FeatureStateStorageWrapper.class);
                        states.put(featureName, featureState);
                    }
                }

                break;
            case NODE_UPDATED:
                String updatedPath = eventData.getPath();
                if (pathHasAFeatureInIt(updatedPath)) {
                    featureName = getFeatureNameFromPath(updatedPath);
                    FeatureStateStorageWrapper featureState = objectMapper.readValue(eventData.getData(), FeatureStateStorageWrapper.class);
                    states.put(featureName, featureState);
                }
                break;
            case NODE_REMOVED:
                featureName = eventData.getPath();
                if (featureName.contains("/")) {
                    break;
                }
                states.remove(featureName);
                break;
            case INITIALIZED:
                initializationLatch.countDown();
            default:
                break;
        }
    }

    private String getFeatureNameFromPath(String updatedPath) {
        String featureName;
        featureName = updatedPath.substring(featuresZnode.length() + 1);
        return featureName;
    }

    private boolean pathHasAFeatureInIt(String updatedPath) {
        return updatedPath.length() > featuresZnode.length();
    }

    public static Builder newBuilder(CuratorFramework curatorFramework, String featuresZnode) {
        return new Builder(curatorFramework, featuresZnode);
    }

    public static class Builder {

        private final CuratorFramework curatorFramework;
        private final String featuresZnode;

        public Builder(CuratorFramework curatorFramework, String featuresZnode) {
            this.curatorFramework = curatorFramework;
            this.featuresZnode = featuresZnode;
        }

        public ZookeeperStateRepository build() throws Exception {
            return new ZookeeperStateRepository(this);
        }
    }
}
