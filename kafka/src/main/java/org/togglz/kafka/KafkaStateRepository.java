package org.togglz.kafka;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.togglz.core.util.FeatureStateStorageWrapper.featureStateForWrapper;
import static org.togglz.core.util.FeatureStateStorageWrapper.wrapperForFeatureState;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.util.FeatureStateStorageWrapper;

/**
 * A {@link StateRepository} based on Apache Kafka. The instances of this class contain
 * a {@link ConcurrentHashMap} for caching {@link FeatureState}s, a {@link KafkaConsumer}
 * for updating the cache and a {@link KafkaProducer} for sending {@link FeatureState}s
 * to Kafka. The method {@link #getFeatureState(Feature)} reads the {@link FeatureState}
 * from the cache and the method {@link #setFeatureState(FeatureState)} invokes the
 * underlying {@link KafkaProducer}.
 *
 * <p>The method {@link #consumerLag()} returns the current lag of the Kafka consumer. If
 * the lag is greater than zero, the {@link FeatureState} cache might contain stale data.
 * Since every newly created instance of this class has to read all {@link FeatureState}s
 * from Kafka, this means that a {@link KafkaStateRepository} should only be used after its
 * initialization has completed. Therefore, the constructor blocks the calling thread until
 * the consumer lag has reached zero. The maximum amount of time that the constructor blocks
 * can be configured with the <i>initializationTimeout</i>. Moreover, it is a good practice to
 * implement a health check that monitors the consumer lag.
 *
 * <p>The underlying Kafka consumer polls for data. The polling interval can be configured
 * with the <i>pollingTimeout</i>. If an error occurs during the processing of records
 * fetched from Kafka, the Kafka consumer will be shutdown. Afterwards, {@link #isRunning()}
 * will return {@code false} and {@link #getFeatureState(Feature)} will return {@code null}.
 *
 * <p>To create a new {@link KafkaStateRepository} instance, it is required to define an
 * inbound topic and an outbound topic. The underlying Kafka consumer fetches data from the
 * inbound topic and the Kafka producer sends data to the outbound topic. In most cases, the
 * inbound topic and the outbound topic will be identical. But depending on the cross datacenter
 * replication strategy, it might be necessary to use different topics and to replicate the data
 * between them. The inbound topic should be configured to use the cleanup policy <i>compact</i>
 * since, on the hand, it is used as persistent storage and, on the other, it has to be entirely
 * consumed during the initialization of the {@link KafkaStateRepository}.
 *
 * @author Florian Stefan
 */
public class KafkaStateRepository implements AutoCloseable, StateRepository {

  private static final Logger LOG = LoggerFactory.getLogger(KafkaStateRepository.class);

  private final Map<String, FeatureStateStorageWrapper> featureStates;
  private final FeatureStateConsumer featureStateConsumer;
  private final FeatureStateProducer featureStateProducer;

  private KafkaStateRepository(Builder builder) {
    featureStates = new ConcurrentHashMap<>();
    featureStateConsumer = new FeatureStateConsumer(builder, this::handleUpdate);
    featureStateProducer = new FeatureStateProducer(builder);

    featureStateConsumer.start();
  }

  /**
   * Returns a new instance of a {@link KafkaStateRepository} builder.
   *
   * @return the new {@link KafkaStateRepository} builder
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public void close() {
    featureStateConsumer.close();
    featureStateProducer.close();
  }

  @Override
  public FeatureState getFeatureState(Feature feature) {
    if (isRunning()) {
      FeatureStateStorageWrapper storageWrapper = featureStates.get(feature.name());

      if (storageWrapper != null) {
        return featureStateForWrapper(feature, storageWrapper);
      } else {
        LOG.warn("Could not find featureState for given feature - fallback to default value.");
        return null;
      }
    } else {
      LOG.warn("FeatureStateConsumer not running anymore - fallback to default value.");
      return null;
    }
  }

  @Override
  public void setFeatureState(FeatureState featureState) {
    FeatureStateStorageWrapper storageWrapper = wrapperForFeatureState(featureState);

    featureStateProducer.send(featureState.getFeature(), storageWrapper);

    handleUpdate(featureState.getFeature().name(), storageWrapper);
  }

  /**
   * Returns {@code true} if the underlying Kafka consumer is running.
   * If the Kafka consumer is not running, the {@link FeatureState} cache
   * is not updated and {@link #getFeatureState(Feature)} might return stale
   * data.
   *
   * @return {@code true} if the underlying Kafka consumer is running.
   */
  public boolean isRunning() {
    return featureStateConsumer.isRunning();
  }

  /**
   * Returns the current lag of the underlying Kafka consumer. If the lag is
   * greater than zero, the {@link FeatureState} cache might contain stale data.
   *
   * @return the current consumer lag
   */
  public long consumerLag() {
    return featureStateConsumer.consumerLag();
  }

  private void handleUpdate(String featureName, FeatureStateStorageWrapper storageWrapper) {
    featureStates.put(featureName, storageWrapper);
  }

  public static class Builder {

    private String bootstrapServers;
    private String inboundTopic;
    private String outboundTopic;
    private Duration pollingTimeout = ofMillis(500);
    private Duration initializationTimeout = ofSeconds(5);

    private Builder() {
    }

    /**
     * Defines a list of host/port pairs that will be used by the underlying Kafka consumer
     * and producer for establishing an initial connection to the Kafka cluster.
     *
     * @param bootstrapServers the list of host/port pairs used for establishing a connection
     * @return the current instance of this builder
     */
    public Builder bootstrapServers(String bootstrapServers) {
      this.bootstrapServers = bootstrapServers;
      return this;
    }

    /**
     * Defines the name of the topic that the underlying Kafka consumer will poll to update
     * the {@link FeatureState} cache. In most cases, the inbound topic and the outbound topic
     * will be identical.
     *
     * @param inboundTopic the name of the topic used by the Kafka consumer
     * @return the current instance of this builder
     */
    public Builder inboundTopic(String inboundTopic) {
      this.inboundTopic = inboundTopic;
      return this;
    }

    /**
     * Defines the name of the topic to which the underlying Kafka producer will send
     * {@link FeatureState}s. In most cases, the inbound topic and the outbound topic
     * will be identical.
     *
     * @param outboundTopic the name of the topic used by the Kafka producer
     * @return the current instance of this builder
     */
    public Builder outboundTopic(String outboundTopic) {
      this.outboundTopic = outboundTopic;
      return this;
    }

    /**
     * Defines the polling interval of the underlying Kafka consumer. A higher value will
     * increase the latency of {@link FeatureState} updates. A lower value will increase
     * the I/O pressure on the Kafka cluster. If no polling timeout is provided, a default
     * value of {@code 500 ms} will be used.
     *
     * @param pollingTimeout the polling interval
     * @return the current instance of this builder
     */
    public Builder pollingTimeout(Duration pollingTimeout) {
      this.pollingTimeout = pollingTimeout;
      return this;
    }

    /**
     * Defines an upper bound for the initialization time of the {@link KafkaStateRepository}.
     * If the underlying Kafka consumer is not able to read all data from Kafka within the
     * given interval, the {@link #build()} method will throw an exception. If no initialization
     * timeout is provided, a default value of {@code 5 s} will be used.
     *
     * @param initializationTimeout the upper bound for the initialization time
     * @return the current instance of this builder
     */
    public Builder initializationTimeout(Duration initializationTimeout) {
      this.initializationTimeout = initializationTimeout;
      return this;
    }

    /**
     * Returns a new {@link KafkaStateRepository} instance.
     *
     * @return the new {@link KafkaStateRepository} instance
     * @throws NullPointerException if any of the configuration values is {@code null}
     * @throws RuntimeException if the {@link KafkaStateRepository} can't be initialized
     *         within the defined interval
     */
    public KafkaStateRepository build() {
      return new KafkaStateRepository(this);
    }

  }

  private static class FeatureStateConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(FeatureStateConsumer.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final KafkaConsumer<String, String> kafkaConsumer;
    private final BiConsumer<String, FeatureStateStorageWrapper> updateHandler;
    private final String inboundTopic;
    private final Duration pollingTimeout;
    private final Map<TopicPartition, Long> offsets;
    private final CountDownLatch initializationLatch;
    private final Duration initializationTimeout;
    private final CountDownLatch shutdownLatch;

    private volatile boolean running;
    private volatile long consumerLag;

    private FeatureStateConsumer(Builder builder, BiConsumer<String, FeatureStateStorageWrapper> updateHandler) {
      Properties properties = new Properties();
      properties.setProperty(BOOTSTRAP_SERVERS_CONFIG, requireNonNull(builder.bootstrapServers));
      properties.setProperty(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      properties.setProperty(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      properties.setProperty(ENABLE_AUTO_COMMIT_CONFIG, "false");

      this.kafkaConsumer = new KafkaConsumer<>(properties);
      this.updateHandler = updateHandler;
      this.inboundTopic = requireNonNull(builder.inboundTopic);
      this.pollingTimeout = requireNonNull(builder.pollingTimeout);
      this.offsets = new ConcurrentHashMap<>();
      this.initializationLatch = new CountDownLatch(1);
      this.initializationTimeout = requireNonNull(builder.initializationTimeout);
      this.shutdownLatch = new CountDownLatch(1);

      running = false;
      consumerLag = Long.MAX_VALUE;
    }

    private synchronized void start() {
      if (running) {
        LOG.info("FeatureStateConsumer has already been started.");
      } else {
        try {
          LOG.debug("Starting to start FeatureStateConsumer.");
          asThreadWithExceptionHandler(this::run).start();
          LOG.debug("Successfully started FeatureStateConsumer.");
          initializationLatch.await(initializationTimeout.toMillis(), MILLISECONDS);
          LOG.debug("Successfully initialized FeatureStateConsumer.");
        } catch (InterruptedException e) {
          throw new RuntimeException("An error occurred while awaiting initialization.", e);
        }
      }
    }

    private synchronized void close() {
      if (running) {
        try {
          LOG.debug("Starting to close FeatureStateConsumer.");
          kafkaConsumer.wakeup();
          shutdownLatch.await();
          LOG.debug("Successfully closed FeatureStateConsumer.");
        } catch (InterruptedException e) {
          LOG.error("An error occurred while closing FeatureStateConsumer.", e);
        }
      } else {
        LOG.info("FeatureStateConsumer has already been closed.");
      }
    }

    private boolean isRunning() {
      return running;
    }

    private long consumerLag() {
      return consumerLag;
    }

    private Thread asThreadWithExceptionHandler(Runnable runnable) {
      Thread thread = new Thread(runnable);

      thread.setUncaughtExceptionHandler((currentThread, throwable) ->
          LOG.error("An uncaught error has been raised.", throwable)
      );

      return thread;
    }

    private void run() {
      assignConsumer();
      running = true;

      try {
        while (true) {
          ConsumerRecords<String, String> records = kafkaConsumer.poll(pollingTimeout);

          processRecords(records);

          updateConsumerLag();
        }
      } catch (WakeupException e) {
        LOG.info("Received shutdown signal.");
      } catch (RuntimeException e) {
        LOG.error("An error occurred while processing feature states: KafkaConsumer will be closed.", e);
      } finally {
        running = false;
        shutdown();
      }
    }

    private void assignConsumer() {
      LOG.debug("Starting to retrieve partitions for topic {}.", inboundTopic);
      List<TopicPartition> topicPartitions = getTopicPartitions();
      LOG.debug("Successfully retrieved topic partitions {}.", topicPartitions);

      kafkaConsumer.assign(topicPartitions);
      kafkaConsumer.seekToBeginning(topicPartitions);
    }

    private List<TopicPartition> getTopicPartitions() {
      List<TopicPartition> topicPartitions = new ArrayList<>();

      for (PartitionInfo partitionInfo : kafkaConsumer.partitionsFor(inboundTopic)) {
        topicPartitions.add(new TopicPartition(partitionInfo.topic(), partitionInfo.partition()));
      }

      return topicPartitions;
    }

    private void processRecords(ConsumerRecords<String, String> records) {
      for (ConsumerRecord<String, String> record : records) {
        String featureName = record.key();
        String featureStateAsString = record.value();

        try {
          LOG.debug("Starting to process state of feature {}.", featureName);
          FeatureStateStorageWrapper storageWrapper = deserialize(featureStateAsString);
          LOG.debug("Successfully deserialized state of feature {}.", featureName);
          updateHandler.accept(featureName, storageWrapper);
          LOG.debug("Successfully processed state of feature {}.", featureName);
          updatePartitionOffset(record.partition(), record.offset());
        } catch (Exception e) {
          throw new RuntimeException("An error occurred while processing state of feature " + featureName + ".", e);
        }
      }
    }

    private FeatureStateStorageWrapper deserialize(String featureStateAsString) throws IOException {
      return MAPPER.readValue(featureStateAsString, FeatureStateStorageWrapper.class);
    }

    private void updatePartitionOffset(int partition, long offset) {
      LOG.debug("Starting to update offset {} of partition {}.", offset, partition);
      offsets.put(new TopicPartition(inboundTopic, partition), offset);
      LOG.debug("Successfully updated offset {} of partition {}.", offset, partition);
    }

    private void updateConsumerLag() {
      consumerLag = accumulateEndOffsets();

      if (consumerLag < 1) {
        initializationLatch.countDown();
      }
    }

    private long accumulateEndOffsets() {
      AtomicLong accumulator = new AtomicLong(0L);

      kafkaConsumer.endOffsets(kafkaConsumer.assignment()).forEach((topicPartition, endOffset) -> {
        if (endOffset > 0L) {
          long oldValue = accumulator.get();
          long partitionOffset = endOffset - offsets.getOrDefault(topicPartition, 0L) - 1L;
          accumulator.set(oldValue + partitionOffset);
        }
      });

      return accumulator.get();
    }

    private void shutdown() {
      try {
        LOG.debug("Starting to close KafkaConsumer.");
        kafkaConsumer.close();
        LOG.debug("Successfully closed KafkaConsumer.");
      } catch (RuntimeException e) {
        LOG.error("An error occurred while closing KafkaConsumer.", e);
      } finally {
        shutdownLatch.countDown();
      }
    }
  }

  private static class FeatureStateProducer {

    private static final Logger LOG = LoggerFactory.getLogger(FeatureStateProducer.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final KafkaProducer<String, String> producer;
    private final String outboundTopic;

    private FeatureStateProducer(Builder builder) {
      Properties properties = new Properties();
      properties.setProperty(BOOTSTRAP_SERVERS_CONFIG, requireNonNull(builder.bootstrapServers));
      properties.setProperty(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
      properties.setProperty(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
      properties.setProperty(ACKS_CONFIG, "all");

      this.producer = new KafkaProducer<>(properties);
      this.outboundTopic = requireNonNull(builder.outboundTopic);
    }

    private void close() {
      try {
        LOG.debug("Starting to close KafkaProducer.");
        producer.close();
        LOG.debug("Successfully closed KafkaProducer.");
      } catch (RuntimeException e) {
        LOG.error("An error occurred while closing KafkaProducer.", e);
      }
    }

    private void send(Feature feature, FeatureStateStorageWrapper storageWrapper) {
      String featureName = feature.name();

      try {
        LOG.debug("Starting to update state of feature {}.", featureName);
        String featureStateAsString = serialize(storageWrapper);
        LOG.debug("Successfully serialized state of feature {}.", featureName);
        producer.send(buildRecord(featureName, featureStateAsString), buildCallback(featureName));
      } catch (Exception e) {
        LOG.error("An error occurred while updating state of feature {}.", featureName, e);
      }
    }

    private String serialize(FeatureStateStorageWrapper storageWrapper) throws IOException {
      return MAPPER.writeValueAsString(storageWrapper);
    }

    private ProducerRecord<String, String> buildRecord(String featureName, String featureStateAsString) {
      return new ProducerRecord<>(outboundTopic, featureName, featureStateAsString);
    }

    private Callback buildCallback(String featureName) {
      return (metadata, exception) -> {
        if (exception == null) {
          LOG.debug("Successfully updated state of feature {}.", featureName);
        } else {
          LOG.error("An error occurred while updating state of feature {}.", featureName, exception);
        }
      };
    }

  }

}
