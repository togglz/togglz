package org.togglz.ws.server;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.togglz.core.Feature;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.file.FileBasedStateRepository;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;
import org.togglz.ws.client.ClientConfiguration.RemoteFeatures;

/**
 * Togglz server configuration.  The file-based repository is configurable and looked-up in the classpath. 
 */
public class ServerConfiguration implements TogglzConfig {

    private String fileRepository;

    public ServerConfiguration(String fileRepository) {
        this.fileRepository = fileRepository;
    }

    public Class<? extends Feature> getFeatureClass() {
        return RemoteFeatures.class;
    }

    public StateRepository getStateRepository() {
        try {
            return new FileBasedStateRepository(new ClassPathResource(fileRepository).getFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to build FileBasedStateRepository", e);
        }
    }

    public UserProvider getUserProvider() {
        return new UserProvider() {
            @Override
            public FeatureUser getCurrentUser() {
                return new SimpleFeatureUser("admin", true);
            }
        };
    }

}