#  Plugin utilities

Repository for utilities to create and test IntelliJ plugins.

The project consists of several modules:

- `kotlin-utilities-plugin` - internal module for different plugins runners to show some examples of using this tool;
- `kotlin-utilities-core` - common utilities for IntelliJ plugins (e.g. base classes for runners);
- `kotlin-utilities-test` - utilities to test IntelliJ plugins;
- `kotlin-utilities-python` - utilities for IntelliJ plugins that work with Python projects;

## Getting started as a library

### From maven repo

```kotlin
repositories {
   mavenCentral()
   mavenLocal()
   maven("https://packages.jetbrains.team/maven/p/big-code/bigcode")
}

val utilitiesProjectName = "org.jetbrains.research.plugin-utilities"
val utilitiesProjectVersion = "1.0"

dependencies {
    implementation("$utilitiesProjectName-core:$utilitiesProjectVersion")
    implementation("$utilitiesProjectName-test:$utilitiesProjectVersion")
    implementation("$utilitiesProjectName-python:$utilitiesProjectVersion")
}
```

### From github repo

You can use this plugin as a library in your plugin by importing it in `settings.gradle.kts` and `build.gradle.kts files`:

1. File `settings.gradle.kts` (in this example we add two modules, but you can import others):

```kotlin
val utilitiesRepo = "https://github.com/JetBrains-Research/plugin-utilities.git"
val utilitiesProjectName = "org.jetbrains.research.pluginUtilities"

sourceControl {
   gitRepository(URI.create(utilitiesRepo)) {
      producesModule("$utilitiesProjectName:plugin-utilities-core")
      producesModule("$utilitiesProjectName:plugin-utilities-test")
   }
}
```

2. File `build.gradle.kts` in the `dependencies` section 
   (in this example we add two modules from the main branch, but you can import others):

```kotlin
val utilitiesProjectName = "org.jetbrains.research.pluginUtilities"
dependencies {
   implementation("$utilitiesProjectName:plugin-utilities-core") {
      version {
         branch = "main"
      }
   }
   implementation("$utilitiesProjectName:plugin-utilities-test") {
      version {
         branch = "main"
      }
   }
}
```

## Running tasks directly

### Preprocessing

Before opening repositories they should be preprocessed. Preprocessing
* removes all `.idea` folders
* adds `local.properties` files with `sdk.dir=<path to Android Sdk>` where it detects a Gradle build system (Gradle Kotlin DSL included).

Preprocessing **DOES NOT** mutate the original dataset but copies it to the output folder.

Suppose you have a dataset with 3 repositories

```
path/to/dataset/
  repo1/
  repo2/
  repo3/
```

You can run preprocessing with 
```shell
./gradlew preprocessKotlinJava -Pinput="path/to/dataset" -Poutput="path/to/output" -PandroidSdk="path/to/androidSdk"
```

which will store preprocessed repositories in the output folder:


```
path/to/output/
  repo1/
  repo2/
  repo3/
```

## Running tests locally

### In Docker

1. There are 2 ways to get our Docker image:
   1. Download it from DockerHub `sudo docker pull furetur/plugin-utilities`
   2. Or build it locally `sudo docker build --tag furetur/plugin-utilities .`
3. Run the tests `sudo docker run -v $(pwd):/repo furetur/plugin-utilities`

### Without Docker

1. Clone this [repository](https://github.com/JetBrains-Research/plugin-utilies-mock-data) somewhere. We will call that folder `<mockdata>`. 
2. Set the environment variable `JAVA_MOCK_PROJECTS=<mockdata>/java_mock_projects`.
3. Now you can build _plugin-utilities_ by running `./gradlew build`. 


## Publishing to maven repo
```shell
gradle plugin-utilities-core:publish
gradle plugin-utilities-python:publish
gradle plugin-utilities-test:publish
```