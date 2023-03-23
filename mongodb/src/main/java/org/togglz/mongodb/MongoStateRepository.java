package org.togglz.mongodb;

import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

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
    protected final WriteConcern writeConcern;

    private MongoStateRepository(Builder builder) {
        this.mongoClient = builder.client;
        this.dbname = builder.dbname;
        this.collection = builder.collection;
        this.writeConcern = builder.writeConcern;
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {

        Document result = (Document) togglzCollection().find(queryFor(feature)).first();

        if (result != null) {

            FeatureState state = new FeatureState(feature);

            boolean enabledValue = result.getBoolean(FIELD_ENABLED, false);
            state.setEnabled(enabledValue);

            String strategyValue = result.getString(FIELD_STRATEGY);
            if (strategyValue != null) {
                state.setStrategyId(strategyValue.trim());
            }

            Object paramsValue = result.get(FIELD_PARAMS);
            if (paramsValue instanceof Document) {
                Document params = (Document) paramsValue;
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

        Document featureStateDocument = new Document()
                .append(FIELD_FEATURE, featureState.getFeature().name())
                .append(FIELD_ENABLED, featureState.isEnabled());

        if (featureState.getStrategyId() != null) {
            featureStateDocument.append(FIELD_STRATEGY, featureState.getStrategyId());
        }

        if (featureState.getParameterNames().size() > 0) {
            Document params = new Document();
            for (String key : featureState.getParameterNames()) {
                params.append(key, featureState.getParameter(key));
            }
            featureStateDocument.append(FIELD_PARAMS, params);
        }

        Document query = queryFor(featureState.getFeature());
        ReplaceOptions replaceOptions = new ReplaceOptions().upsert(true);

        togglzCollection().replaceOne(query, featureStateDocument, replaceOptions);
    }

    protected Document queryFor(Feature feature) {
        return new Document(FIELD_FEATURE, feature.name());
    }

    protected MongoCollection togglzCollection() {
        MongoDatabase db = mongoClient.getDatabase(dbname);
        return db.getCollection(collection).withWriteConcern(writeConcern);
    }

    /**
     * Creates a new builder for creating a {@link MongoStateRepository}.
     *
     * @param client the client instance to use for connecting to MongoDB
     * @param dbname the database name used for storing features state.
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
        private String collection = "togglz";
        private WriteConcern writeConcern = WriteConcern.ACKNOWLEDGED;

        /**
         * Creates a new builder for a {@link MongoStateRepository}.
         *
         * @param client the client instance to use for connecting to MongoDB
         * @param dbname the database name used for storing features state.
         */
        public Builder(MongoClient client, String dbname) {
            this.client = client;
            this.dbname = dbname;
        }

        /**
         * The name of the collection used by the repository to store the feature state. The default is <code>togglz</code>.
         *
         * @param collection The name of the collection to use
         */
        public Builder collection(String collection) {
            this.collection = collection;
            return this;
        }

        /**
         * The {@link WriteConcern} used when accessing the database. By default <code>ACKNOWLEDGED</code> will be used.
         *
         * @param writeConcern The {@link WriteConcern} to use
         */
        public Builder writeConcern(WriteConcern writeConcern) {
            this.writeConcern = writeConcern;
            return this;
        }

        /**
         * Creates a new {@link MongoStateRepository} using the current settings.
         */
        public MongoStateRepository build() {
            return new MongoStateRepository(this);
        }

    }
}
