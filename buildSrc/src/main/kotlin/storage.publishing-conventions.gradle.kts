plugins {
  id("storage.common-conventions")
  `maven-publish`
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      pom.withXml {
        val repositoriesNode = asNode().appendNode("repositories")
        var index = 0

        project.repositories.forEach {
          val repositoryName = it.name

          if (repositoryName == "MavenLocal" || repositoryName == "MavenRepo") {
            return@forEach
          }

          repositoriesNode.appendNode("repository").apply {
            val id = if (repositoryName == "maven") "repository-${++index}" else repositoryName

            appendNode("id", id)
            appendNode("url", (it as MavenArtifactRepository).url.toString())
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