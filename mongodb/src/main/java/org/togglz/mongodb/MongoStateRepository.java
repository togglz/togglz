package org.togglz.mongodb;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

/**
 * <p>
 * A state repository which stores the feature state in a MongoDB database.
 * </p>
 * 
 * <p>
 * The class provides a builder which can be used to configure the repository:
 * </p>
 * 
 * <pre>
 * StateRepository repository = MongoStateRepository
 *     .newBuilder(mongoClient, &quot;mydb&quot;)
 *     .collection(&quot;togglz&quot;)
 *     .authentication(&quot;john&quot;, &quot;tiger&quot;)
 *     .writeConcern(WriteConcern.REPLICA_ACKNOWLEDGED)
 *     .build();
 * </pre>
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
    protected final String username;
    protected final char[] password;
    protected final WriteConcern writeConcert;

    private MongoStateRepository(Builder builder) {
        this.mongoClient = builder.client;
        this.dbname = builder.dbname;
        this.collection = builder.collection;
        this.username = builder.username;
        this.password = builder.password;
        this.writeConcert = builder.writeConcern;
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
        DB db = mongoClient.getDB(dbname);
        db.setWriteConcern(writeConcert);
        if (username != null && password != null) {
            db.authenticate(username, password);
        }
        return db.getCollection(collection);
    }

    /**
     * Creates a new builder for creating a {@link MongoStateRepository}.
     */
    public static Builder newBuilder(MongoClient client, String dbname) {
        return new Builder(client, dbname);
    }

    /**
     * Builder for a {@link MongoStateRepository}.
     */
    public static class Builder {

        private final MongoClient client;
        private final String dbname;
        private String collection;
        private String username;
        private char[] password;
        private WriteConcern writeConcern;

        public Builder(MongoClient client, String dbname) {
            this.client = client;
            this.dbname = dbname;
        }

        public Builder collection(String collection) {
            this.collection = collection;
            return this;
        }

        public Builder authentication(String username, String password) {
            return authentication(username, password.toCharArray());
        }

        public Builder authentication(String username, char[] password) {
            this.username = username;
            this.password = password;
            return this;
        }

        public Builder writeConcern(WriteConcern writeConcern) {
            this.writeConcern = writeConcern;
            return this;
        }

        public MongoStateRepository build() {
            return new MongoStateRepository(this);
        }

    }

}
