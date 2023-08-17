package org.togglz.googlecloudspanner.repository;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.ReadOnlyTransaction;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.Statement;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class SpannerEmulatorControl {
    public static final String TEST_PROJECT_ID = "test-project";
    public static final String TEST_INSTANCE_ID = "test-instance";
    public static final String TEST_DATABASE_ID = "test-database";

    private GenericContainer spannerContainer;
    private Spanner spanner;

    public void start() {
        File dockerFile = new File("src/test/docker/Dockerfile");
        if (!dockerFile.isFile()) {
            throw new IllegalStateException("Dockerfile not found: " + dockerFile.getAbsolutePath());
        }
        spannerContainer = new GenericContainer(
                new ImageFromDockerfile().withFileFromPath(".", dockerFile.getParentFile().toPath())
                        .get())
                .withExposedPorts(9010, 9020)
                .withEnv("PROJECT_ID", TEST_PROJECT_ID)
                .withEnv("INSTANCE_ID", TEST_INSTANCE_ID)
                .withEnv("DATABASE_ID", TEST_DATABASE_ID)
                .withReuse(true);
        this.spannerContainer.start();

        SpannerOptions options = SpannerOptions.newBuilder()
                .setProjectId("test-project")
                .setEmulatorHost("localhost:" + spannerContainer.getMappedPort(9010))
                .build();
        this.spanner = options.getService();

        waitForSpannerAvailable(60, TimeUnit.SECONDS);
    }

    private void waitForSpannerAvailable(int duration, TimeUnit durationUnit) {
        long waitMillis = durationUnit.toMillis(duration);
        long startMillis = System.currentTimeMillis();
        while (System.currentTimeMillis() < startMillis + waitMillis) {
            if (!isSpannerAvailable()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Spanner not available within %d %s.".formatted(duration, durationUnit));
                }
            }
        }
    }

    private boolean isSpannerAvailable() {
        try {
            try (ReadOnlyTransaction tx = getDatabaseClient().singleUseReadOnlyTransaction()) {
                try (ResultSet resultSet = tx.executeQuery(Statement.newBuilder("select 1").build())) {
                    return resultSet.next();
                }
            }
        } catch (Exception ex) {
            return false;
        }
    }

    public DatabaseClient getDatabaseClient() {
        DatabaseId db = getDatabaseId();
        return spanner.getDatabaseClient(db);
    }

    public DatabaseId getDatabaseId() {
        return DatabaseId.of(TEST_PROJECT_ID, TEST_INSTANCE_ID, TEST_DATABASE_ID);
    }

    public void stop() {
        if (spanner != null && !spanner.isClosed()) {
            this.spanner.close();
        }

        if (spannerContainer != null && spannerContainer.isRunning()) {
            this.spannerContainer.stop();
        }

        this.spanner = null;
        this.spannerContainer = null;
    }
}

