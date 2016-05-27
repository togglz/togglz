package org.togglz.zookeeper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ZookeeperStateRepository implements StateRepository, TreeCacheListener {
    private Logger log = LoggerFactory.getLogger(ZookeeperStateRepository.class);

    private static final String FEAURES_PATH = "/features";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private TreeCache treeCache;
    private ConcurrentMap<String, FeatureStateStorageWrapper> states;

    protected final CuratorFramework curatorFramework;
    protected final String znode;

    private ZookeeperStateRepository(Builder builder) throws Exception {
        this.curatorFramework = builder.curatorFramework;
        this.znode = builder.znode;

        initializeFeaturePath();

        states = new ConcurrentHashMap<>();
        treeCache = new TreeCache(curatorFramework, znode);
        treeCache.getListenable().addListener(this);
        treeCache.start();
    }

    private void initializeFeaturePath() {
        try {
            curatorFramework.createContainers(znode + FEAURES_PATH);
        } catch (Exception e) {
            throw new RuntimeException("couldn't initialize the zookeeper state repository", e);
        }
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        return featureStateForWrapper(feature, states.get(feature.name()));
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        FeatureStateStorageWrapper wrapper = wrapperForFeatureState(featureState);
        try {
            String json = objectMapper.writeValueAsString(wrapper);
            String path = znode + FEAURES_PATH + "/" + featureState.getFeature().name();
            curatorFramework.createContainers(path);
            curatorFramework.setData().forPath(path, json.getBytes("UTF-8"));
            states.put(featureState.getFeature().name(), wrapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent event) throws Exception {
        switch (event.getType()) {
            case NODE_ADDED:
                String addedPath = event.getData().getPath();
                if (addedPath.contains(FEAURES_PATH + "/")) {
                    String featureName = addedPath.split(FEAURES_PATH + "/")[1];
                    if (featureName.contains("/")) {
                        break;
                    }
                    if (event.getData().getData().length > 0) {
                        FeatureStateStorageWrapper featureState = objectMapper.readValue(event.getData().getData(), FeatureStateStorageWrapper.class);
                        states.putIfAbsent(featureName, featureState);
                    }
                }
                break;
            case NODE_UPDATED:
                String updatedPath = event.getData().getPath();
                if (updatedPath.contains(FEAURES_PATH + "/")) {
                    String featureName = updatedPath.split(FEAURES_PATH + "/")[1];
                    if (featureName.contains("/")) {
                        break;
                    }
                    FeatureStateStorageWrapper featureState = objectMapper.readValue(event.getData().getData(), FeatureStateStorageWrapper.class);
                    states.put(featureName, featureState);
                }
                break;
            case NODE_REMOVED:
                String removedPath = event.getData().getPath();
                if (removedPath.contains(FEAURES_PATH + "/")) {
                    String featureName = removedPath.split(FEAURES_PATH + "/")[1];
                    if (featureName.contains("/")) {
                        break;
                    }
                    states.remove(featureName);
                }
                break;
            case INITIALIZED:
                initializeFeatures();
            default:
                break;
        }
    }

    private void initializeFeatures() throws Exception {
        List<String> features = curatorFramework.getChildren().forPath(znode + FEAURES_PATH);
        for (String feature : features) {
            byte[] featureData = curatorFramework.getData().forPath(znode + FEAURES_PATH + feature);
            states.put(feature, objectMapper.readValue(featureData, FeatureStateStorageWrapper.class));
        }
    }

    private FeatureStateStorageWrapper wrapperForFeatureState(FeatureState featureState) {
        FeatureStateStorageWrapper wrapper = new FeatureStateStorageWrapper();
        wrapper.setEnabled(featureState.isEnabled());
        wrapper.setStrategyId(featureState.getStrategyId());
        wrapper.getParameters().putAll(featureState.getParameterMap());

        return wrapper;
    }

    private FeatureState featureStateForWrapper(Feature feature, FeatureStateStorageWrapper wrapper) {
        FeatureState featureState = new FeatureState(feature);
        featureState.setEnabled(wrapper.isEnabled());
        featureState.setStrategyId(wrapper.getStrategyId());
        for (Map.Entry<String,String> e : wrapper.getParameters().entrySet()) {
            featureState.setParameter(e.getKey(), e.getValue());
        }

        return featureState;
    }

    public static Builder newBuilder(CuratorFramework curatorFramework, String znode) {
        return new Builder(curatorFramework, znode);
    }

    public static class Builder {

        private final CuratorFramework curatorFramework;
        private final String znode;

        public Builder(CuratorFramework curatorFramework, String znode) {
            this.curatorFramework = curatorFramework;
            this.znode = znode;
        }

        public ZookeeperStateRepository build() throws Exception {
            return new ZookeeperStateRepository(this);
        }
    }
}
