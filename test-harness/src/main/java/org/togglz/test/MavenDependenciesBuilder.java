package org.togglz.test;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;

public class MavenDependenciesBuilder {

    private final List<String> artifacts = new ArrayList<>();
    private final String filesystemPomPath = Paths.get("").toAbsolutePath().resolve("pom.xml").toString();

    public MavenDependenciesBuilder artifact(String artifact) {
        artifacts.add(artifact);
        return this;
    }

    public File[] asFiles() {
        return Maven.resolver().loadPomFromFile(filesystemPomPath).resolve(artifacts).withTransitivity().asFile();
    }

}
