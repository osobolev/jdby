plugins {
    `lib`
}

dependencies {
    api(project(":jdbq-core"))
    api(project(":jdbq-dao"))

    "manualRuntimeOnly"("org.postgresql:postgresql:42.7.8")
}
