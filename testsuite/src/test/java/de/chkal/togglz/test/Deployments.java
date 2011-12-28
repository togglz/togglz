package de.chkal.togglz.test;

import java.io.File;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;

public class Deployments {

    public static WebArchive getServletArchive() {
        return ShrinkWrap
                .create(WebArchive.class, "test.war")
                .addAsLibraries(
                        getTogglzCoreArchive(),
                        getTogglzSerlvetArchive())
                .addAsLibraries(
                        DependencyResolvers.use(MavenDependencyResolver.class)
                                .artifact("org.slf4j:slf4j-jdk14:1.6.4")
                                .resolveAs(JavaArchive.class))
                .addClass(FeatureServlet.class);
    }

    public static WebArchive getCDIArchive() {
        return getServletArchive()
                .addAsLibrary(getTogglzCDIArchive())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    public static WebArchive getSpringArchive() {
        return getServletArchive()
                .addAsLibrary(getTogglzSpringArchive())
                .addAsLibraries(
                        DependencyResolvers.use(MavenDependencyResolver.class)
                                .artifact("org.springframework:spring-web:3.0.7.RELEASE")
                                .resolveAs(JavaArchive.class))
                .addClass(FeatureServlet.class)
                .addAsWebInfResource("common/spring/applicationContext.xml")
                .setWebXML("common/spring/spring-web.xml");        
    }
    
    private static JavaArchive getTogglzSerlvetArchive() {
        return ShrinkWrap.create(ZipImporter.class, "togglz-servlet.jar")
                .importFrom(new File("../servlet/target/togglz-servlet-1.0-SNAPSHOT.jar"))
                .as(JavaArchive.class);
    }
    
    private static JavaArchive getTogglzCoreArchive() {
        return ShrinkWrap.create(ZipImporter.class, "togglz-core.jar")
                .importFrom(new File("../core/target/togglz-core-1.0-SNAPSHOT.jar"))
                .as(JavaArchive.class);
    }

    private static JavaArchive getTogglzSpringArchive() {
        return ShrinkWrap.create(ZipImporter.class, "togglz-spring.jar")
                .importFrom(new File("../spring/target/togglz-spring-1.0-SNAPSHOT.jar"))
                .as(JavaArchive.class);
    }

    private static JavaArchive getTogglzCDIArchive() {
        return ShrinkWrap.create(ZipImporter.class, "togglz-cdi.jar")
                .importFrom(new File("../cdi/target/togglz-cdi-1.0-SNAPSHOT.jar"))
                .as(JavaArchive.class);
    }
    
}
