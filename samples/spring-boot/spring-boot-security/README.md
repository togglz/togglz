# Spring Boot Security Sample

This Spring Security web application sample demonstrates enabling/disabling features based on various users.
The admin console is only accessable by admin users as configured via the feature-admin-authority application property.

See `sample.Application` and `features.properties` for different users and their feature settings.

Run `mvn clean spring-boot:run`

The sample project also contains `MockMvc` integration tests.
