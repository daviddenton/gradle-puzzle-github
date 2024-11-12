import groovy.namespace.QName
import groovy.util.Node

// This plugin collects artifacts from the dependent projects to publish everything in a fat Jar
plugins {
    id("java-library")
    id("maven-publish")
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            name = "local"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }

    publications {
        val archivesBaseName = tasks.named<Jar>("jar").get().archiveBaseName.get()
        create<MavenPublication>("mavenJava") {
            artifactId = archivesBaseName
            versionMapping {
                allVariants {
                    fromResolutionResult()
                }
            }
            pom.withXml {
                asNode().appendNode("name", archivesBaseName)
                asNode().appendNode("description", description)
                asNode().appendNode("url", "https://http4k.org")
                asNode().appendNode("developers")
                    .appendNode("developer").appendNode("name", "Ivan Sanchez").parent()
                    .appendNode("email", "ivan@http4k.org")
                    .parent().parent()
                    .appendNode("developer").appendNode("name", "David Denton").parent()
                    .appendNode("email", "david@http4k.org")
                asNode().appendNode("scm")
                    .appendNode("url", "https://github.com/http4k/http4k").parent()
                    .appendNode("connection", "scm:git:git@github.com:http4k/http4k.git").parent()
                    .appendNode("developerConnection", "scm:git:git@github.com:http4k/http4k.git")
                asNode().appendNode("licenses").appendNode("license")
                    .appendNode("name", "Apache License, Version 2.0").parent()
                    .appendNode("url", "http://www.apache.org/licenses/LICENSE-2.0.html")
            }
            from(components["java"])

            // replace all runtime dependencies with provided
            pom.withXml {
                asNode()
                    .childrenCalled("dependencies")
                    .flatMap { it.childrenCalled("dependency") }
                    .flatMap { it.childrenCalled("scope") }
                    .forEach { if (it.text() == "runtime") it.setValue("provided") }
            }
        }

    }
}


fun Node.childrenCalled(wanted: String) = children()
    .filterIsInstance<Node>()
    .filter {
        val name = it.name()
        (name is QName) && name.localPart == wanted
    }

