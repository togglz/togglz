package org.togglz.zookeeper;

import com.google.gson.Gson;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ZookeeperStateRepository implements StateRepository, TreeCacheListener {
    private static final Logger log = LoggerFactory.getLogger(ZookeeperStateRepository.class);

    private TreeCache treeCache;
    private ConcurrentMap<String, FeatureState> states;

    protected final CuratorFramework curatorFramework;
    protected final String znode;

    private ZookeeperStateRepository(Builder builder) {
        this.curatorFramework = builder.curatorFramework;
        this.znode = builder.znode;

        states = new ConcurrentHashMap<>();
        treeCache = new TreeCache(curatorFramework, znode);
        treeCache.getListenable().addListener(this);
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        return states.get(feature.name());
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        Gson gson = new Gson();
        String json = gson.toJson(featureState);

        try {
            curatorFramework.setData().forPath(znode + "/features/" + featureState.getFeature().name(), json.getBytes());
            states.replace(featureState.getFeature().name(), featureState);
        } catch (Exception e) {
            log.error("could not persist FeatureState to Zookeeper", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {

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
    }
}
