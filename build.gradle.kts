plugins {
  java
  `maven-publish`
}

subprojects {
  apply(plugin = "java-library")
  apply(plugin = "maven-publish")

  repositories {
    mavenLocal()
    maven("https://repo.revengenetwork.es/repository/libs/") {
      name = "fenixRepository"
      credentials(PasswordCredentials::class)
    }
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