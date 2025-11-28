plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
}

rootProject.name = "jdbq"

include("jdbq-core")
include("jdbq-testing")
include("jdbq-sample")
