plugins {
    `java`
    `application`
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation("io.github.osobolev.jdby:jdby-core:1.1")

    runtimeOnly("com.h2database:h2:2.4.240")

    testImplementation("io.github.osobolev.jdby:jdby-testing:1.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.1")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "jdby.sample.Example"
}

tasks.withType(JavaCompile::class).configureEach {
    options.encoding = "UTF-8"
    options.release.set(17)
    options.compilerArgs.add("-parameters")
}

tasks.named<Test>("test").configure {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
    }
}
