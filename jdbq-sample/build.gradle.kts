plugins {
    `base-lib`
}

dependencies {
    implementation(project(":jdbq-core"))

    runtimeOnly("com.h2database:h2:2.2.224")
}
