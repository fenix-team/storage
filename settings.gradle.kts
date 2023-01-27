rootProject.name = "storage"

arrayOf(
    "api", "api-codec", "caffeine-dist", "mongo-legacy-dist",
    "redis-dist", "gson-dist"
).forEach {
    includePrefixed(it)
}

fun includePrefixed(name: String) {
    val kebabName = name.replace(':', '-')
    val path = name.replace(':', '/')
    val baseName = "${rootProject.name}-$kebabName"

    include(baseName)
    project(":$baseName").projectDir = file(path)
}