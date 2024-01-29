import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    `java-library`
    id("maven-publish")
    id("signing")
}

group = "io.github.spencerpark"
version = "2.3.0"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(group = "org.zeromq", name = "jeromq", version = "0.5.1")
    api(group = "com.google.code.gson", name = "gson", version = "2.8.5")

    testImplementation(group = "junit", name = "junit", version = "4.12")
    testImplementation(group = "org.hamcrest", name = "hamcrest-all", version = "1.3")

    testImplementation(group = "com.google.jimfs", name = "jimfs", version = "1.1")
}

java {
    withJavadocJar()
    withSourcesJar()

    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

tasks.processResources {
    val tokens = mapOf(
        "version" to project.version,
        "project" to project.name,
    )
    inputs.properties(tokens)

    filesMatching("kernel-metadata.json") {
        filter(mapOf("tokens" to tokens), ReplaceTokens::class.java)
    }
}

publishing {
    repositories {
        maven {
            name = "sonatype"

            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            url = if (project.hasProperty("release")) releasesRepoUrl else snapshotsRepoUrl

            credentials {
                // ossrhUsername and ossrhPassword are defined in the global gradle.properties on the
                // machine uploading the artifacts (including the keys). If not present only `publish`
                // should fail, not `publishToMavenLocal`. Essentially defer the failure as long as possible.
                username = project.findProperty("ossrhUsername") as? String
                password = project.findProperty("ossrhPassword") as? String
            }
        }

        // For publishing to `build/repos/{releases,snapshots}` to test the artifacts look as expected.
        maven {
            name = "buildFolder"

            val releasesRepoUrl = layout.buildDirectory.dir("repos/releases").get().asFile.toURI()
            val snapshotsRepoUrl = layout.buildDirectory.dir("repos/snapshots").get().asFile.toURI()
            url = if (project.hasProperty("release")) releasesRepoUrl else snapshotsRepoUrl
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name = "Jupyter JVM Base Kernel"
                packaging = "jar"
                description =
                    "An abstract kernel implementation for Jupyter kernels running on the Java virtual machine."
                url = "https://github.com/SpencerPark/jupyter-jvm-basekernel"

                scm {
                    url = "https://github.com/SpencerPark/jupyter-jvm-basekernel.git"
                    connection = "scm:git:https://github.com/SpencerPark/jupyter-jvm-basekernel.git"
                    developerConnection = "scm:git:git@github.com:SpencerPark/jupyter-jvm-basekernel.git"
                }

                licenses {
                    license {
                        name = "MIT License"
                        url = "http://opensource.org/licenses/MIT"
                        distribution = "repo"
                    }
                }

                developers {
                    developer {
                        id = "SpencerPark"
                        name = "Spencer Park"
                        email = "spinnr95@gmail.com"
                    }
                }
            }
        }
    }

    signing {
        isRequired = project.hasProperty("release") && gradle.taskGraph.hasTask("publish")
        sign(publishing.publications["mavenJava"])
    }
}
