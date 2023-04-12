import com.diffplug.gradle.spotless.FormatExtension

plugins {
  java
  `maven-publish`
  id("net.kyori.indra") version "3.0.1"
  id("net.kyori.indra.checkstyle") version "3.0.1"
  id("com.diffplug.spotless") version "6.18.0"
}

subprojects {
  apply(plugin = "java-library")
  apply(plugin = "maven-publish")
  apply(plugin = "net.kyori.indra")
  apply(plugin = "net.kyori.indra.checkstyle")
  apply(plugin = "com.diffplug.spotless")

  repositories {
    mavenLocal()
    maven("https://repo.revengenetwork.es/repository/libs/") {
      name = "fenixRepository"
      credentials(PasswordCredentials::class)
    }
  }

  indra {
    javaVersions {
      target(17)
      minimumToolchain(17)
    }
    checkstyle("10.8.0")
  }

  spotless {
    fun FormatExtension.applyCommon() {
      trimTrailingWhitespace()
      indentWithSpaces(2)
    }
    java {
      importOrder("", "\\#")
      removeUnusedImports()
      applyCommon()
    }
    kotlinGradle {
      applyCommon()
    }
  }

  dependencies {
    checkstyle("ca.stellardrift:stylecheck:0.2.0")
  }

  tasks {
    compileJava {
      dependsOn("spotlessApply")
      dependsOn("checkstyleMain")
      options.compilerArgs.add("-parameters")
    }
  }

  publishing {
    publications {
      create<MavenPublication>("maven") {
        from(components["java"])
      }
    }

    repositories {
      maven {
        name = "fenixRepository"
        val repositoryUrl = "https://repo.revengenetwork.es/repository/libs-${
          if (version.toString().endsWith("SNAPSHOT")) "snapshots" else "releases"
        }/"
        url = uri(repositoryUrl)
        credentials(PasswordCredentials::class)
      }
    }
  }
}