plugins {
    `java`
    `application`
    id("com.github.ben-manes.versions") version "0.53.0"
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
    implementation("io.github.osobolev.jdby:jdby-core:1.3")

    runtimeOnly("com.h2database:h2:2.4.240")

    testImplementation("io.github.osobolev.jdby:jdby-testing:1.3")
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")

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

tasks.withType(com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask::class).configureEach {
    resolutionStrategy {
        componentSelection {
            all(Action<com.github.benmanes.gradle.versions.updates.resolutionstrategy.ComponentSelectionWithCurrent> {
                if (candidate.version.contains("-a")) {
                    reject("Alpha version")
                } else if (candidate.version.contains("-b")) {
                    reject("Beta version")
                } else if (candidate.version.contains("-M")) {
                    reject("Milestone version")
                }
            })
        }
    }
}
