package org.togglz.spring.boot.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.togglz.core.repository.cache.CachingStateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.s3.S3StateRepository;
import org.togglz.spring.boot.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TogglzStateRepositoryAutoConfigurationTest extends BaseTest {

    @Test
    void shouldCreateS3StateRepository() {
        contextRunnerWithFeatureProviderConfig()
                .withPropertyValues("togglz.state-repository.s3.bucket-name: test")
                .run((context -> assertThat(context.getBean(S3StateRepository.class)).isNotNull()));
    }

    @Test
    void shouldNotCreateS3StateRepositoryWithoutConfiguredBucketName() {
        contextRunnerWithFeatureProviderConfig()
                .run((context -> assertThatThrownBy(() -> context.getBean(S3StateRepository.class)).isExactlyInstanceOf(NoSuchBeanDefinitionException.class)));
    }

    @Test
    void shouldNotCreateS3StateRepositoryIfAnotherStateRepositoryIsPresent() {
        contextRunnerWithFeatureProviderConfig()
                .withPropertyValues("togglz.state-repository.s3.bucket-name: test")
                .withBean("someOtherRepository", InMemoryStateRepository.class)
                .run(context -> {
                    assertThatThrownBy(() -> context.getBean(S3StateRepository.class)).isExactlyInstanceOf(NoSuchBeanDefinitionException.class);
                    assertThat(context.getBean(InMemoryStateRepository.class)).isNotNull();
                });
    }

    @Test
    void shouldWrapS3StateRepositoryWithCache() {
        contextRunnerWithFeatureProviderConfig()
                .withPropertyValues("togglz.state-repository.s3.bucket-name: test")
                .withPropertyValues("togglz.state-repository.cache.enabled: true")
                .withPropertyValues("togglz.state-repository.cache.time-to-live: 1m")
                .run((context -> assertThat(context.getBean(CachingStateRepository.class)).isNotNull()));
    }
}
