import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    id("base-lib")
    id("com.vanniktech.maven.publish")
}

group = "io.github.osobolev.jdby"
version = "1.4"

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("${project.group}", "${project.name}", "${project.version}")
    configure(JavaLibrary(
        javadocJar = JavadocJar.Javadoc(),
        sourcesJar = SourcesJar.Sources()
    ))
}

mavenPublishing.pom {
    name = "${project.group}:${project.name}"
    description = provider { project.description }
    url = "https://github.com/osobolev/jdby"
    licenses {
        license {
            name = "The Apache License, Version 2.0"
            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
        }
    }
    developers {
        developer {
            name = "Oleg Sobolev"
            organizationUrl = "https://github.com/osobolev"
        }
    }
    scm {
        connection = "scm:git:https://github.com/osobolev/jdby.git"
        developerConnection = "scm:git:https://github.com/osobolev/jdby.git"
        url = "https://github.com/osobolev/jdby"
    }
}
