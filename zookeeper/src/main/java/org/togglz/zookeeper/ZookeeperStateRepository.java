package org.togglz.zookeeper;

import com.google.gson.Gson;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ZookeeperStateRepository implements StateRepository, TreeCacheListener {

    private static final String FEAURES_PATH = "/features";

    private static final Gson gson = new Gson();

    private TreeCache treeCache;
    private ConcurrentMap<String, FeatureState> states;

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
        return states.get(feature.name());
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        String json = gson.toJson(featureState);

        try {
            String path = znode + FEAURES_PATH + "/" + featureState.getFeature().name();
            curatorFramework.createContainers(path);
            curatorFramework.setData().forPath(path, json.getBytes());
            states.replace(featureState.getFeature().name(), featureState);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent event) throws Exception {
        switch (event.getType()) {
            case NODE_ADDED:
                String addedPath = event.getData().getPath();
                if (addedPath.contains(FEAURES_PATH)) {
                    String featureName = addedPath.split(FEAURES_PATH)[1];
                    if (featureName.contains("/")) {
                        break;
                    }
                    FeatureState featureState = gson.fromJson(new String(event.getData().getData()), FeatureState.class);
                    states.putIfAbsent(featureName, featureState);
                }
                break;
            case NODE_UPDATED:
                String updatedPath = event.getData().getPath();
                if (updatedPath.contains(FEAURES_PATH)) {
                    String featureName = updatedPath.split(FEAURES_PATH)[1];
                    if (featureName.contains("/")) {
                        break;
                    }
                    FeatureState featureState = gson.fromJson(new String(event.getData().getData()), FeatureState.class);
                    states.replace(featureName, featureState);
                }
                break;
            case NODE_REMOVED:
                String removedPath = event.getData().getPath();
                if (removedPath.contains(FEAURES_PATH)) {
                    String featureName = removedPath.split(FEAURES_PATH)[1];
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
            byte[] featureData = curatorFramework.getData().forPath(znode + FEAURES_PATH + "/" + feature);
            states.put(feature, gson.fromJson(new String(featureData), FeatureState.class));
        }
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
