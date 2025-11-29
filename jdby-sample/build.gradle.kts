plugins {
    `base-lib`
    `application`
}

dependencies {
    implementation(project(":jdby-core"))

    runtimeOnly("com.h2database:h2:2.4.240")

    testImplementation(project(":jdby-testing"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.1")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "jdby.sample.Example"
}
