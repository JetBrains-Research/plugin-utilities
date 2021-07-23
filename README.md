#  Plugin utilities

Repository for utilities to create and test IntelliJ plugins.

The project consists of several modules:

- `kotlin-utilities-plugin` - internal module for different plugins runners to show some examples of using this tool;
- `kotlin-utilities-core` - common utilities for IntelliJ plugins (e.g. base classes for runners);
- `kotlin-utilities-test` - utilities to test IntelliJ plugins;
- `kotlin-utilities-python` - utilities for IntelliJ plugins that work with Python projects;

## Getting started as a library

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
