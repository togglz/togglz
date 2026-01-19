package org.togglz.spring.boot.repository;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.cache.CachingStateRepository;
import org.togglz.s3.S3StateRepository;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzAutoConfiguration;
import software.amazon.awssdk.services.s3.S3Client;

@AutoConfiguration(before = TogglzAutoConfiguration.class)
@EnableConfigurationProperties(TogglzStateRepositoryConfigurationProperties.class)
public class TogglzStateRepositoryAutoConfiguration {

    private final TogglzStateRepositoryConfigurationProperties configurationProperties;

    public TogglzStateRepositoryAutoConfiguration(TogglzStateRepositoryConfigurationProperties configurationProperties) {
        this.configurationProperties = configurationProperties;
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({S3StateRepository.class, S3Client.class})
    @ConditionalOnProperty(prefix = "togglz.state-repository.s3", name = "bucket-name")
    public class S3StateRepositoryConfiguration {

        @ConditionalOnMissingBean
        @Bean
        public S3Client s3Client() {
            return S3Client.create();
        }

        @ConditionalOnMissingBean
        @Bean
        public StateRepository stateRepository(S3Client s3Client) {
            StateRepository s3StateRepository = S3StateRepository
                    .newBuilder(s3Client, configurationProperties.getS3().getBucketName())
                    .build();

            return wrapWithCache(s3StateRepository);
        }
    }

    private StateRepository wrapWithCache(StateRepository delegate) {
        if (configurationProperties.getCache().isEnabled()) {
            return new CachingStateRepository(delegate, configurationProperties.getCache().getTimeToLive().toMillis());
        } else {
            return delegate;
        }
    }
}
