plugins {
  id("storage.common-conventions")
  `maven-publish`
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
      url = uri("https://maven.pkg.github.com/fenix-team/packages")
      credentials(PasswordCredentials::class)
    }
  }
}