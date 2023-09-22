plugins {
  id("storage.common-conventions")
}

dependencies {
  api(project(":storage-api-codec"))
  api(libs.mongo)
}
