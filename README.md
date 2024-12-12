# ApplicationProvider 
[![Maven Central Version](https://img.shields.io/maven-central/v/net.kibotu/ApplicationProvider)](https://central.sonatype.com/artifact/net.kibotu/ApplicationProvider) [![](https://jitpack.io/v/kibotu/ApplicationProvider.svg)](https://jitpack.io/#kibotu/ApplicationProvider) [![Android CI](https://github.com/kibotu/ApplicationProvider/actions/workflows/android.yml/badge.svg)](https://github.com/kibotu/ApplicationProvider/actions/workflows/android.yml) [![API](https://img.shields.io/badge/Min%20API-23%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=23) [![API](https://img.shields.io/badge/Target%20API-35%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=35) [![API](https://img.shields.io/badge/Java-17-brightgreen.svg?style=flat)](https://www.oracle.com/java/technologies/javase/17all-relnotes.html)

# Retrieve the android application from anywhere
Useful to develop a standalone library

# How to install

## MavenCentral

```groovy 
allprojects {
    repositories {
        mavenCentral()
    }
}

dependencies {
    implementation 'net.kibotu:ApplicationProvider:{latest-version}'
}

```

## Jitpack

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.kibotu:ApplicationProvider:{latest-version}'
}
```

```kotlin
//from anywhere
val application = ApplicationProvider.application
```

# Retrieve the current activity from anywhere
```kotlin
//from anywhere
val currentActivity : Activity? = ActivityProvider.currentActivity()
```

Or safety from a kotlin coroutine context : 

```kotlin
launch {
    val currentActivity = ActivityProvider.activity() //cannot be null
    Log.d(TAG, "activity : $currentActivity")
}
```

## Listen for current activity

```kotlin
launch {
    ActivityProvider.listenCurrentActivity().collect {
        Log.d(TAG, "activity : $currentActivity")
    }
}
```

# Providers

If you need to execute a code automatically when the application starts, without adding it into your application's class code

Create a class that extends `Provider`
```kotlin
class StethoProvider : Provider() {
    override fun provide() {
        val application = ApplicationProvider.application //if you need the application context
        Stetho.initializeWithDefaults(application)
    }
}
```

Add it into your manifest
```xml
<provider
     android:name=".StethoProvider"
     android:authorities="${applicationId}.StethoInitializer" />
```

# Initializers

You do not need to override the Application now

# Before

```kotlin
class MyApplication : Application() {
    override fun onCreate(){
        Stetho.initializeWithDefaults(application)
    }
}
```

# After

## Using a provider (for libraries)

*Note that you can include it directly on your library's aar*

```kotlin
class StethoInitializer : ProviderInitializer() {
    override fun initialize(): (Application) -> Unit = {
        Stetho.initializeWithDefaults(application)
    }
}
```

```xml
<provider
     android:name=".timber.TimberInitializer"
     android:authorities="${applicationId}.StethoInitializer" />
```

## Using an initializer

Another way using Initializer

```kotlin
val InitializeStetho by lazy {
    ApplicationProvider.listen { application ->
        Stetho.initializeWithDefaults(application)
    }
}

class MainActivity : AppCompatActivity() {

    init {
        InitializeStetho
    }

    override fun onCreate(savedInstanceState: Bundle?) {
    ...
    }
}
```

Follow me on Twitter: [![Twitter Follow](https://img.shields.io/twitter/follow/wolkenschauer.svg?style=social)](https://twitter.com/wolkenschauer)

Let me know what you think: [jan.rabe@kibotu.net](mailto:jan.rabe@kibotu.net)

Contributions welcome!

