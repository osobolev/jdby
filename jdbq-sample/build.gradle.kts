plugins {
    `base-lib`
}

dependencies {
    implementation(project(":jdbq-core"))

    runtimeOnly("com.h2database:h2:2.2.224")

    testImplementation(project(":jdbq-testing"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.1")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
