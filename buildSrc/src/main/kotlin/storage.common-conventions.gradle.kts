plugins {
  `java-library`
}

repositories {
  mavenLocal()
  mavenCentral()
}

tasks {
  java {
    toolchain {
      languageVersion.set(JavaLanguageVersion.of(17))
    }
  }

  compileJava {
    options.compilerArgs.add("-parameters")
  }
}