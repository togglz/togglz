package org.togglz.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

/**
 * <p>
 * A state repository which stores the feature state in a Hazelcast distributed map.
 * </p>
 * 
 * <p>
 * The class provides a builder which can be used to configure the Hazelcast instance and map to be used:
 * </p>
 * 
 * <pre>
 * StateRepository repository = HazelcastStateRepository.newBuilder().mapName(&quot;my_map&quot;)
 * 		.config(hazelcastConfig).build();
 * </pre>
 * 
 * @author Camiel de Vleeschauwer
 */
public class HazelcastStateRepository implements StateRepository {

	protected final HazelcastInstance hazelcastInstance;
	protected final Config hazelcastConfig;
	protected final ClientConfig hazelcastClientConfig;
	protected final String mapName;

	public HazelcastStateRepository(Config hazelcastConfig, String mapName) {
		this.mapName = mapName;
		this.hazelcastConfig = hazelcastConfig;
		this.hazelcastClientConfig = null;
		this.hazelcastInstance = createHazelcastInstance();
	}

	public HazelcastStateRepository(ClientConfig hazelcastClientConfig, String mapName) {
		this.mapName = mapName;
		this.hazelcastConfig = null;
		this.hazelcastClientConfig = hazelcastClientConfig;
		this.hazelcastInstance = createHazelcastInstance();
	}

	public HazelcastStateRepository(HazelcastInstance hazelcastInstance, String mapName) {
		this.mapName = mapName;
		this.hazelcastConfig = null;
		this.hazelcastClientConfig = null;
		this.hazelcastInstance = hazelcastInstance;
	}
	
	private HazelcastStateRepository(Builder builder) {
		mapName = builder.mapName;
		hazelcastConfig = builder.hazelcastConfig;
		hazelcastClientConfig = builder.hazelcastClientConfig;
		this.hazelcastInstance = builder.hazelcastInstance!=null ? builder.hazelcastInstance
				: createHazelcastInstance();
	}

	private HazelcastInstance createHazelcastInstance() {
		if(hazelcastClientConfig==null && hazelcastConfig==null) {
			return Hazelcast.newHazelcastInstance();
		}

		if(hazelcastClientConfig==null) {
			return Hazelcast.newHazelcastInstance(hazelcastConfig);
		}

		return HazelcastClient.newHazelcastClient(hazelcastClientConfig);
	}

	@Override
	public FeatureState getFeatureState(final Feature feature) {
		final IMap<Feature, FeatureState> map = hazelcastInstance.getMap(mapName);
		return map.get(feature);
	}

	@Override
	public void setFeatureState(final FeatureState featureState) {
		final IMap<Feature, FeatureState> map = hazelcastInstance.getMap(mapName);
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
		private ClientConfig hazelcastClientConfig = null;
		private HazelcastInstance hazelcastInstance = null;

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

		public Builder clientConfig(ClientConfig hazelcastClientConfig) {
			this.hazelcastClientConfig = hazelcastClientConfig;
			return this;
		}

		public Builder hazelcastInstance(HazelcastInstance hazelcastInstance) {
			this.hazelcastInstance = hazelcastInstance;
			return this;
		}

		/**
		 * Creates a new {@link HazelcastStateRepository} using the current
		 * settings.
		 */
		public HazelcastStateRepository build() {
		    checkStateOfWhatIsSet();
			return new HazelcastStateRepository(this);
		}

        private void checkStateOfWhatIsSet() {
		    int countOfWhatIsSet = 0;
		    if(hazelcastClientConfig!=null) {
		        countOfWhatIsSet++;
            }
		    if(hazelcastConfig!=null) {
		        countOfWhatIsSet++;
            }
		    if(hazelcastInstance!=null) {
		        countOfWhatIsSet++;
            }

            if(countOfWhatIsSet>1) {
                throw new IllegalStateException("Please only configure exactly one of hazelcastClientConfig, hazelcastConfig, or hazelcastInstance");
            }
        }

    }

}
