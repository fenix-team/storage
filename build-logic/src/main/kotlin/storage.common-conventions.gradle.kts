import com.diffplug.gradle.spotless.FormatExtension

plugins {
  id("storage.base-conventions")
  id("net.kyori.indra.crossdoc")
  id("net.kyori.indra")
  id("net.kyori.indra.checkstyle")
  id("net.kyori.indra.licenser.spotless")
}

val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

dependencies {
  api(platform(project(":storage-bom")))
  checkstyle(libs.stylecheck)
}

spotless {
  fun FormatExtension.applyCommon() {
    trimTrailingWhitespace()
    endWithNewline()
    indentWithSpaces(2)
  }
  java {
    importOrderFile(rootProject.file(".spotless/fenix.importorder"))
    applyCommon()
  }
  kotlinGradle {
    applyCommon()
  }
}

indraCrossdoc {
  baseUrl().set(providers.gradleProperty("javadocPublishRoot"))
  nameBasedDocumentationUrlProvider {
    projectNamePrefix.set("storage-")
  }
}

tasks {
  compileJava {
    dependsOn("spotlessApply")
    options.compilerArgs.add("-parameters")
  }
}
