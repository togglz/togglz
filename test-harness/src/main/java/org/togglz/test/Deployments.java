package org.togglz.test;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class Deployments {

    public static WebArchive getBasicWebArchive() {
        return ShrinkWrap
            .create(WebArchive.class, "test.war")
            .addAsLibraries(
                getTogglzCoreArchive(),
                getTogglzServletArchive())
            .addClass(FeatureServlet.class)
            .addClass(UserServlet.class);
    }

    public static JavaArchive getTogglzServletArchive() {
        return ShrinkWrap.create(ExplodedImporter.class, "togglz-servlet.jar")
            .importDirectory("../servlet/target/classes")
            .as(JavaArchive.class);
    }

    public static JavaArchive getTogglzCoreArchive() {
        return ShrinkWrap.create(ExplodedImporter.class, "togglz-core.jar")
            .importDirectory("../core/target/classes")
            .as(JavaArchive.class);
    }

    public static JavaArchive getTogglzSpringArchive() {
        return ShrinkWrap.create(ExplodedImporter.class, "togglz-spring.jar")
            .importDirectory("../spring-core/target/classes")
            .importDirectory("../spring-web/target/classes")
            .as(JavaArchive.class);
    }

    public static JavaArchive getTogglzGuiceArchive() {
        return ShrinkWrap.create(ExplodedImporter.class, "togglz-guice.jar")
            .importDirectory("../guice/target/classes")
            .as(JavaArchive.class);
    }

    public static JavaArchive getTogglzCDIArchive() {
        return ShrinkWrap.create(ExplodedImporter.class, "togglz-cdi.jar")
            .importDirectory("../cdi/target/classes")
            .as(JavaArchive.class);
    }

    public static JavaArchive getTogglzShiroArchive() {
        return ShrinkWrap.create(ExplodedImporter.class, "togglz-shiro.jar")
            .importDirectory("../shiro/target/classes")
            .as(JavaArchive.class);
    }
}
