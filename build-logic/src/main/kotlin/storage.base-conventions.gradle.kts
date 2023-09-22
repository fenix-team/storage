plugins {
  id("net.kyori.indra.publishing")
}

val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

indra {
  javaVersions {
    target(11)
    minimumToolchain(11)
    strictVersions(true)
  }
  checkstyle(libs.versions.checkstyle.get())

  github("fenix-team", "storage") {
    ci(true)
  }
  mitLicense()

  signWithKeyFromPrefixedProperties("fenix")
  configurePublications {
    pom {
      developers {
        developer {
          id.set("pixeldev")
          name.set("Angel Miranda")
          url.set("https://github.com/pixeldev")
          email.set("pixel@fenixteam.org")
        }
      }
    }
  }
}
