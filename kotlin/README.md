Kotlin wrapper for togglz library.

It's not possible to use the `Feature`-Interface for enum features as in Java, because it's `name`-method clashes with the buildin `name`-method of the enum class.

Therefore this wrapper uses a plain enum without implementing `Feature` and provides a `FeatureProvider` to wrap it into a Feature. 

# Usage (with spring)

Import Dependency: 

`implementation("org.togglz:kotlin:2.7.0-SNAPSHOT")`

Create an enum for your feature togglz but don't extend the Togglz-Feature interface:

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

Create a spring config that creates a `FeatureManager`and a `FeatureProvider`:

```
@Configuration
class MyTogglzConfiguration {

    @Bean
    fun featureProvider() = EnumClassFeatureProvider(KotlinTestFeatures::class.java)

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

}
```

##Enable all togglz

for unit tests:
```
val featureManager = createFeatureManagerForTest(KotlinTestFeatures::class)
KFeatureManagerSupport.allEnabledFeatureConfig(featureManager)
```


for spring acceptance tests:
```
@Autowired
val featureManager: FeatureManager
....
KFeatureManagerSupport.allEnabledFeatureConfig(featureManager)
```

##Enable one toggle

```
KFeatureManagerSupport.enable(Feature { KotlinTestFeatures.BAR.name })
```


# Credentials

Inspired by and copied from https://github.com/e-breuninger/spring-boot-starter-breuninger/tree/master/togglz
