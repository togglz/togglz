package org.togglz.spring.boot.repository;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "togglz.state-repository")
public class TogglzStateRepositoryConfigurationProperties {

    private final S3 s3 = new S3();

    private final Cache cache = new Cache();


    public S3 getS3() {
        return s3;
    }

    public Cache getCache() {
        return cache;
    }

    public static class S3 {

        private String bucketName;

        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }
    }

    public static class Cache {

        private boolean enabled = false;

        private Duration timeToLive = Duration.ofMinutes(1);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getTimeToLive() {
            return timeToLive;
        }

        public void setTimeToLive(Duration timeToLive) {
            this.timeToLive = timeToLive;
        }
    }
}
