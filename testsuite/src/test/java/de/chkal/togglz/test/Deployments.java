package de.chkal.togglz.test;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
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
                .addClass(FeatureServlet.class)
                .addClass(UserServlet.class);
    }

    public static WebArchive getCDIArchive() {
        return getServletArchive()
                .addAsLibrary(getTogglzCDIArchive())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    public static WebArchive getSeamSecurityArchive() {
        return getCDIArchive()
                .addAsLibraries(DependencyResolvers.use(MavenDependencyResolver.class)
                        .artifact("org.jboss.seam.security:seam-security:3.1.0.Final")
                        .artifact("joda-time:joda-time:1.6.2")
                        .resolveAs(JavaArchive.class))
                .addAsLibraries(getTogglzSeamSecurityArchive());
    }

    public static WebArchive getJSFArchive() {
        return getCDIArchive()
                .addAsLibrary(getTogglzJSFArchive())
                .addAsWebInfResource("jsf/jsf-web.xml", "web.xml")
                .addAsWebInfResource("jsf/faces-config.xml", "faces-config.xml");
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
        return ShrinkWrap.create(ExplodedImporter.class, "togglz-servlet.jar")
                .importDirectory("../servlet/target/classes")
                .as(JavaArchive.class);
    }

    private static JavaArchive getTogglzCoreArchive() {
        return ShrinkWrap.create(ExplodedImporter.class, "togglz-core.jar")
                .importDirectory("../core/target/classes")
                .as(JavaArchive.class);
    }

    private static JavaArchive getTogglzSpringArchive() {
        return ShrinkWrap.create(ExplodedImporter.class, "togglz-spring.jar")
                .importDirectory("../spring/target/classes")
                .as(JavaArchive.class);
    }

    private static JavaArchive getTogglzCDIArchive() {
        return ShrinkWrap.create(ExplodedImporter.class, "togglz-cdi.jar")
                .importDirectory("../cdi/target/classes")
                .as(JavaArchive.class);
    }

    private static JavaArchive getTogglzJSFArchive() {
        return ShrinkWrap.create(ExplodedImporter.class, "togglz-jsf.jar")
                .importDirectory("../jsf/target/classes")
                .as(JavaArchive.class);
    }

    private static JavaArchive getTogglzSeamSecurityArchive() {
        return ShrinkWrap.create(ExplodedImporter.class, "togglz-seam-security.jar")
                .importDirectory("../seam-security/target/classes")
                .as(JavaArchive.class);
    }

}
