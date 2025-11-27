plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
}

rootProject.name = "jdbq"

fun add(name: String, path: String) {
    val dir = file(path)
    include(name)
    project(":${name}").projectDir = dir
}

add("jdbq", "jdbq-core")
add("jdbq-testing", "jdbq-testing")
