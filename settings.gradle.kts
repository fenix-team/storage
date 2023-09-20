pluginManagement {
  includeBuild("build-logic")
}

rootProject.name = "storage-parent"

sequenceOf("api", "api-codec", "caffeine-dist", "gson-dist", "mongo-legacy-dist", "redis-dist").forEach {
  include("storage-$it")
  project(":storage-$it").projectDir = file(it)
}