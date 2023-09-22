plugins {
  id("storage.common-conventions")
}

dependencies {
  api(project(":storage-api"))
  api(libs.caffeine)
}
