plugins {
    `java-library`
    id("maven-publish")
    id("signing")
}

group = "io.github.spencerpark"
version = "3.0.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // TODO not really necessary, would be good to drop it from the runtime dependencies if possible.
    api(libs.gson)

    testImplementation(testLibs.junit)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(testLibs.hamcrest)
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

tasks.test {
    useJUnitPlatform()
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
                name = "Jupyter JVM Base Kernel Runtime"
                packaging = "jar"
                description =
                    "A set of Jupyter utilities and conventions for user code executed by kernels build on jupyter-jvm-basekernel."
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
