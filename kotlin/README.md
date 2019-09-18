# ft3_tokklz
Kotlin wrapper for togglz library.

# Usage (with spring)

Import Dependency: 

`implementation("org.togglz:kotlin:2.7.0-SNAPSHOT")`

Create an enum for your feature togglz but don't extend the Togglz-Features interface:

```
 enum class KotlinTestFeatures {
    @EnabledByDefault
    FOO,

    @Label("bar feature")
    BAR;

    fun isActive(): Boolean {
        return FeatureContext.getFeatureManager().isActive { name }
    }
}
```

Create a spring config that creates a FeatureProvider:

```
@Configuration
class MyTogglzConfiguration {

    @Bean
    @Primary
    fun myFeatureManager(stateRepository: StateRepository,
                              userProvider: UserProvider,
                              featureProvider: FeatureProvider): FeatureManager {

        val featureManager = FeatureManagerBuilder()
                .featureProvider(featureProvider)
                .stateRepository(stateRepository)
                .userProvider(userProvider)
                .build()

        StaticFeatureManagerProvider.setFeatureManager(featureManager)
        KFeatureManagerProvider.featureMgr = featureManager
        return featureManager
    }

    @Configuration
    class FeatureProviderConfiguration {
        @Bean
        fun featureProvider() = EnumClassFeatureProvider(KotlinTestFeatures::class.java)
    }
}
```


# Credentials

Inspired by and copied from https://github.com/e-breuninger/spring-boot-starter-breuninger/tree/master/togglz
