plugins {
  `kotlin-dsl`
}

dependencies {
  implementation(libs.build.indra)
  implementation(libs.build.indra.crossdoc)
  implementation(libs.build.indra.publishing)
  implementation(libs.build.indra.spotless)
  compileOnly(files(libs::class.java.protectionDomain.codeSource.location))
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

repositories {
  maven(url = "https://repo.stellardrift.ca/repository/internal/") {
    name = "stellardriftReleases"
    mavenContent { releasesOnly() }
  }
  maven(url = "https://repo.stellardrift.ca/repository/snapshots/") {
    name = "stellardriftSnapshots"
    mavenContent { snapshotsOnly() }
  }
  gradlePluginPortal()
}

kotlin {
  target {
    compilations.configureEach {
      kotlinOptions {
        jvmTarget = "11"
      }
    }
  }
}
