plugins {
  id("storage.common-conventions")
}

dependencies {
  api(project(":storage-api"))
  api(project(":storage-codec"))
  api(libs.mongo)
}
