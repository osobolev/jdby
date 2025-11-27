plugins {
    `lib`
}

dependencies {
    api(project(":jdbq"))

    "manualRuntimeOnly"("org.postgresql:postgresql:42.7.8")
}
