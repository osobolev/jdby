plugins {
    `base-lib`
}

dependencies {
    implementation(project(":jdbq-core"))
    implementation(project(":jdbq-dao"))

    runtimeOnly("com.h2database:h2:2.2.224")
}
