plugins {
  id("storage.common-conventions")
  `maven-publish`
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      pom.withXml {
        val repositoriesNode = asNode().appendNode("repositories")

        project.repositories.withType(MavenArtifactRepository::class).forEach {
          val repositoryName = it.name

          if (repositoryName == "MavenLocal" || repositoryName == "MavenRepo") {
            return@forEach
          }

          repositoriesNode.appendNode("repository").apply {
            appendNode("id", repositoryName)
            appendNode("url", it.url.toString())
          }
        }
      }

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