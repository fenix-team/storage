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
            name = "stargirlsRepository"
            url = uri("https://maven.pkg.github.com/sg-server/packages")
            credentials(PasswordCredentials::class)
        }
    }
}