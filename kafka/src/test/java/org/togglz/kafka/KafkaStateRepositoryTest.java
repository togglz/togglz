package org.togglz.kafka;

import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.togglz.kafka.KafkaStateRepositoryTest.TestFeatures.FEATURE_A;
import static org.togglz.kafka.KafkaStateRepositoryTest.TestFeatures.FEATURE_B;
import static org.togglz.kafka.KafkaStateRepositoryTest.TestFeatures.UNUSED_FEATURE;

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.activation.UsernameActivationStrategy;
import org.togglz.core.repository.FeatureState;

public class KafkaStateRepositoryTest {

  private static final String TOPIC = "feature-states";
  private static final String EMPTY_TOPIC = "no-feature-states";
  private static final Duration POLLING_TIMEOUT = Duration.ofMillis(200);

  @ClassRule
  public static final SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

  @BeforeClass
  public static void createTopics() {
    createTopic(TOPIC);
    createTopic(EMPTY_TOPIC);

    try (KafkaStateRepository stateRepository = createStateRepository(TOPIC)) {
      for (int i = 0; i < 100; i++) {
        TestFeatures feature = ThreadLocalRandom.current().nextBoolean() ? FEATURE_A : FEATURE_B;
        boolean enabled = ThreadLocalRandom.current().nextBoolean();

        stateRepository.setFeatureState(newFeatureState(feature).setEnabled(enabled));
      }
    }
  }

  @Test
  public void shouldReturnNullWhenTopicIsEmpty() {
    KafkaStateRepository stateRepository = createStateRepository(EMPTY_TOPIC);

    FeatureState receivedFeatureState = stateRepository.getFeatureState(FEATURE_A);

    assertThat(receivedFeatureState).isNull();
  }

  @Test
  public void shouldReturnNullWhenFeatureHasNotBeenUsedYet() {
    KafkaStateRepository stateRepository = createStateRepository(TOPIC);

    stateRepository.setFeatureState(newFeatureState(FEATURE_B).setEnabled(false));
    FeatureState receivedFeatureState = stateRepository.getFeatureState(UNUSED_FEATURE);

    assertThat(receivedFeatureState).isNull();
  }

  @Test
  public void shouldUpdateFeatureStateWithSingleStateRepository() {
    KafkaStateRepository stateRepository = createStateRepository(TOPIC);
    FeatureState featureState = newFeatureState(FEATURE_A).setEnabled(false);

    FeatureState receivedFeatureState = setAndGetFeatureState(stateRepository, featureState);

    assertThat(receivedFeatureState).isEqualToComparingFieldByField(featureState);
  }

  @Test
  public void shouldUpdateFeatureStateWithMultipleStateRepositories() {
    KafkaStateRepository sendingRepository = createStateRepository(TOPIC);
    KafkaStateRepository pollingRepository = createStateRepository(TOPIC);
    FeatureState featureState = newFeatureState(FEATURE_B).setEnabled(true);

    FeatureState receivedFeatureState = setAndGetFeatureState(sendingRepository, pollingRepository, featureState);

    assertThat(receivedFeatureState).isEqualToComparingFieldByField(featureState);
  }

  @Test
  public void shouldUpdateFeatureStateWithActivationStrategy() {
    KafkaStateRepository sendingRepository = createStateRepository(TOPIC);
    KafkaStateRepository pollingRepository = createStateRepository(TOPIC);
    FeatureState featureState = newFeatureStateWithActivationStrategy(FEATURE_A).setEnabled(true);

    FeatureState receivedFeatureState = setAndGetFeatureState(sendingRepository, pollingRepository, featureState);

    assertThat(receivedFeatureState).isEqualToComparingFieldByField(featureState);
  }

  @Test
  public void shouldBeRunningAfterInitialization() {
    KafkaStateRepository stateRepository = createStateRepository(TOPIC);

    assertThat(stateRepository.isRunning()).isTrue();
  }

  @Test
  public void shouldNotBeRunningAfterKafkaConsumerCrashed() {
    String topicName = createTopicName();
    createTopic(topicName);
    KafkaStateRepository stateRepository = createStateRepository(topicName);

    sendRecordAndAwaitNextPoll(topicName, FEATURE_A, "invalid_value");

    assertThat(stateRepository.isRunning()).isFalse();
  }

  private static String createTopicName() {
    return randomUUID().toString();
  }

  private static void createTopic(String topicName) {
    sharedKafkaTestResource.getKafkaTestUtils().createTopic(topicName, 5, (short) 1);
  }

  private static KafkaStateRepository createStateRepository(String topic) {
    return KafkaStateRepository.builder()
        .bootstrapServers(sharedKafkaTestResource.getKafkaConnectString())
        .inboundTopic(topic)
        .outboundTopic(topic)
        .pollingTimeout(POLLING_TIMEOUT)
        .initializationTimeout(Duration.ofSeconds(1))
        .build();
  }

  private static FeatureState newFeatureState(Feature feature) {
    return new FeatureState(feature);
  }

  private static FeatureState newFeatureStateWithActivationStrategy(Feature feature) {
    return new FeatureState(feature)
        .setStrategyId(UsernameActivationStrategy.ID)
        .setParameter(UsernameActivationStrategy.PARAM_USERS, "user1, user2, user3");
  }

  private static FeatureState setAndGetFeatureState(
      KafkaStateRepository stateRepository,
      FeatureState featureState
  ) {
    stateRepository.setFeatureState(featureState);
    return stateRepository.getFeatureState(featureState.getFeature());
  }

  private static FeatureState setAndGetFeatureState(
      KafkaStateRepository sendingRepository,
      KafkaStateRepository pollingRepository,
      FeatureState featureState
  ) {
    sendingRepository.setFeatureState(featureState);
    awaitNextPoll();
    return pollingRepository.getFeatureState(featureState.getFeature());
  }

  private static void sendRecordAndAwaitNextPoll(String topicName, Feature feature, String value) {
    Map<byte[], byte[]> record = new HashMap<>();

    record.put(feature.name().getBytes(), value.getBytes());

    sharedKafkaTestResource.getKafkaTestUtils().produceRecords(record, topicName, 0);

    awaitNextPoll();
  }

  private static void awaitNextPoll() {
    try {
      MILLISECONDS.sleep(2 * POLLING_TIMEOUT.toMillis() + 100);
    } catch (InterruptedException ignored) {
    }
  }

  public enum TestFeatures implements org.togglz.core.Feature {
    FEATURE_A, FEATURE_B, UNUSED_FEATURE
  }

}
