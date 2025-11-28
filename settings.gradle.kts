plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
}

rootProject.name = "jdby"

include("jdby-core")
include("jdby-testing")
include("jdby-sample")
