plugins {
    `lib`
}

dependencies {
    api(project(":jdbq-core"))

    "manualRuntimeOnly"("org.postgresql:postgresql:42.7.8")
}
