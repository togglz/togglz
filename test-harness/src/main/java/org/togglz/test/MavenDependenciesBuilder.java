package org.togglz.test;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
import org.togglz.core.util.Strings;

public class MavenDependenciesBuilder {

    private final List<String> artifacts = new ArrayList<>();
    private String filesystemPomPath;

    public MavenDependenciesBuilder artifact(String artifact) {
        artifacts.add(artifact);
        return this;
    }

    public MavenDependenciesBuilder filesystemRelativePomPath() {
        this.filesystemPomPath = Paths.get("").toAbsolutePath().resolve("pom.xml").toString();
        return this;
    }

    public MavenDependenciesBuilder filesystemPomPath(String filesystemPomPath) {
        this.filesystemPomPath = filesystemPomPath;
        return this;
    }

    public File[] asFiles() {
        MavenResolverSystem resolver = Maven.resolver();
        if (Strings.isNotEmpty(filesystemPomPath)) {
            return  resolver.loadPomFromFile(filesystemPomPath).resolve(artifacts).withTransitivity().asFile();
        }
        return resolver.resolve(artifacts).withTransitivity().asFile();
    }

}
