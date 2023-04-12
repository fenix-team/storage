plugins {
  java
  `maven-publish`
  id("net.kyori.indra") version "3.0.1"
  id("net.kyori.indra.checkstyle") version "3.0.1"
}

subprojects {
  apply(plugin = "java-library")
  apply(plugin = "maven-publish")
  apply(plugin = "net.kyori.indra")
  apply(plugin = "net.kyori.indra.checkstyle")

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

  dependencies {
    checkstyle("ca.stellardrift:stylecheck:0.2.0")
  }

  tasks {
    compileJava {
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