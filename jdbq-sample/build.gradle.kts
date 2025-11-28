plugins {
    `base-lib`
}

dependencies {
    api(project(":jdbq-core"))

    runtimeOnly("com.h2database:h2:2.2.224")
}
