# Examples of using plugin-utilities repo

This folder contains some examples with runners for opening different projects in the headless mode:

- for JVM projects (Java, Kotlin), please, see [JvmProjectOpenerStarter](./src/main/kotlin/org/jetbrains/research/pluginUtilities/examples/JvmProjectOpenerStarter.kt).
To run this starter, please run `ioCli` Gradle task with thw following arguments:
  `-Prunner=jvm-project-opener-runner-example -Pinput=<path to input forder with Kotlin/Java projects> -Poutput=<path to output forder for results>`

- for Python projects, please, see [PythonProjectOpenerStarter](./src/main/kotlin/org/jetbrains/research/pluginUtilities/examples/PythonProjectOpenerStarter.kt).
  To run this starter, please run `ioCli` Gradle task with thw following arguments:
  `-Prunner=python-project-opener-runner-example -Pinput=<path to input forder with Python projects> -Poutput=<path to output forder for results>`

