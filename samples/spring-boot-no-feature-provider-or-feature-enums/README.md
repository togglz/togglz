# Spring Boot No Feature Provider Or Feature Enums Sample

This web application sample includes the `togglz-spring-boot-starter` dependency,
but does neither provide a `FeatureProvider` bean nor sets the `togglz.feature-enums` property.
This results in a dummy feature provider being created and a warning during startup.

Run `mvn clean spring-boot:run`

The sample project also contains `MockMvc` integration tests.
