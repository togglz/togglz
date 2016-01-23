package org.togglz.hazelcast;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

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
 * StateRepository repository = HazelcastStateRepository.newBuilder(mongoClient, &quot;mydb&quot;).collection(&quot;togglz&quot;)
 * 		.authentication(&quot;john&quot;, &quot;tiger&quot;).writeConcern(WriteConcern.REPLICA_ACKNOWLEDGED).build();
 * </pre>
 * 
 * @author Camiel de Vleeschauwer
 */
public class HazelcastStateRepository implements StateRepository {

	protected final HazelcastInstance hazelcastInstance;
	protected final Config hazelcastConfig;
	protected final String mapName;

	public HazelcastStateRepository(Config hazelcastConfig, String mapName)
	{
		this.mapName = mapName;
		this.hazelcastConfig = hazelcastConfig;
		hazelcastInstance=createHazelcastInstance();
	}
	
	private HazelcastStateRepository(Builder builder) {

		mapName = builder.mapName;
		hazelcastConfig = builder.hazelcastConfig;
		hazelcastInstance=createHazelcastInstance();
	}

	private HazelcastInstance createHazelcastInstance() {
		if (hazelcastConfig != null) {
			return Hazelcast.newHazelcastInstance(hazelcastConfig);

		} else {
			return Hazelcast.newHazelcastInstance();
		}
	}

	@Override
	public FeatureState getFeatureState(Feature feature) {

		IMap<Feature, FeatureState> map = hazelcastInstance.getMap(mapName);

		FeatureState featureState = map.get(feature);

		return featureState;

	}

	@Override
	public void setFeatureState(FeatureState featureState) {

		IMap<Feature, FeatureState> map = hazelcastInstance.getMap(mapName);

		map.set(featureState.getFeature(), featureState);
	}

	/**
	 * Creates a new builder for creating a {@link HazelcastStateRepository}.
	 * 
	 */
	public static Builder newBuilder() {
		return new Builder();
	}

	/**
	 * Creates a new builder for creating a {@link HazelcastStateRepository}.
	 * 
	 * @param mapName
	 *            the Hazelcast map name
	 */
	public static Builder newBuilder(String mapName) {
		return new Builder(mapName);
	}

	/**
	 * Builder for a {@link HazelcastStateRepository}.
	 */
	public static class Builder {

		private String mapName = "togglz";
		private Config hazelcastConfig = null;

		/**
		 * Creates a new builder for a {@link HazelcastStateRepository}.
		 * 
		 */
		public Builder() {
		}

		/**
		 * Creates a new builder for a {@link HazelcastStateRepository}.
		 * 
		 * @param mapName
		 *            the Hazelcast map name to use for feature state store
		 */
		public Builder(String mapName) {
			this.mapName = mapName;
		}

		/**
		 * Creates a new builder for a {@link HazelcastStateRepository}.
		 * 
		 * @param hazelcastConfig
		 *            the Hazelcast configuration {@link Config}
		 */
		public Builder(Config hazelcastConfig) {
			this.hazelcastConfig = hazelcastConfig;
		}

		/**
		 * Sets the Hazelcast map name to use.
		 * 
		 * @param mapName
		 *            the Hazelcast map name to use for feature state store
		 */
		public Builder mapName(String mapName) {
			this.mapName = mapName;
			return this;
		}

		/**
		 * Sets the Hazelcast configuration.
		 * 
		 * @param hazelcastConfig
		 *            the Hazelcast configuration {@link Config}
		 */
		public Builder config(Config hazelcastConfig) {
			this.hazelcastConfig = hazelcastConfig;
			return this;
		}

		/**
		 * Creates a new {@link HazelcastStateRepository} using the current
		 * settings.
		 */
		public HazelcastStateRepository build() {
			return new HazelcastStateRepository(this);
		}

	}

}
