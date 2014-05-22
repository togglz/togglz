package org.togglz.test;

public class Packaging {

    public static WebAppDescriptorBuilder webAppDescriptor() {
        return new WebAppDescriptorBuilder();
    }

    public static MavenDependenciesBuilder mavenDependencies() {
        return new MavenDependenciesBuilder();
    }

}
