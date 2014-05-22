package org.togglz.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;

public class MavenDependenciesBuilder {

    private final List<String> artifacts = new ArrayList<String>();

    public MavenDependenciesBuilder artifact(String artifact) {
        artifacts.add(artifact);
        return this;
    }

    public File[] asFiles() {
        return Maven.resolver().resolve(artifacts).withTransitivity().asFile();
    }

}
