package org.togglz.mongodb;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * A state repository which stores the feature state in a MongoDB database.
 * 
 * @author Christian Kaltepoth
 */
public class MongoStateRepository implements StateRepository {

    protected static final String FIELD_FEATURE = "feature";
    protected static final String FIELD_ENABLED = "enabled";
    protected static final String FIELD_STRATEGY = "strategy";
    protected static final String FIELD_PARAMS = "params";

    protected final MongoClient mongoClient;
    protected final String dbname;
    protected final String collection;

    public MongoStateRepository(MongoClient mongoClient, String dbname) {
        this(mongoClient, dbname, "togglz");
    }

    public MongoStateRepository(MongoClient mongoClient, String dbname, String collection) {
        this.mongoClient = mongoClient;
        this.dbname = dbname;
        this.collection = collection;
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {

        DBObject result = togglzCollection().findOne(queryFor(feature));

        if (result != null) {

            FeatureState state = new FeatureState(feature);

            Object enabledValue = result.get(FIELD_ENABLED);
            if (enabledValue instanceof Boolean) {
                state.setEnabled(((Boolean) enabledValue).booleanValue());
            }

            Object strategyValue = result.get(FIELD_STRATEGY);
            if (strategyValue != null) {
                state.setStrategyId(strategyValue.toString().trim());
            }

            Object paramsValue = result.get(FIELD_PARAMS);
            if (paramsValue instanceof DBObject) {
                DBObject params = (DBObject) paramsValue;
                for (String key : params.keySet()) {
                    state.setParameter(key, params.get(key).toString().trim());
                }

            }

            return state;

        }

        return null;

    }

    @Override
    public void setFeatureState(FeatureState featureState) {

        BasicDBObject obj = new BasicDBObject()
            .append(FIELD_FEATURE, featureState.getFeature().name())
            .append(FIELD_ENABLED, featureState.isEnabled());

        if (featureState.getStrategyId() != null) {
            obj.append(FIELD_STRATEGY, featureState.getStrategyId());
        }

        if (featureState.getParameterNames().size() > 0) {
            BasicDBObject params = new BasicDBObject();
            for (String key : featureState.getParameterNames()) {
                params.append(key, featureState.getParameter(key));
            }
            obj.append(FIELD_PARAMS, params);
        }

        DBObject query = queryFor(featureState.getFeature());

        togglzCollection().update(query, obj, true, false);

    }

    protected DBObject queryFor(Feature feature) {
        return new BasicDBObject()
            .append(FIELD_FEATURE, feature.name());
    }

    protected DBCollection togglzCollection() {
        return mongoClient.getDB(dbname).getCollection(collection);
    }

}
