rootProject.name = "jupyter-jvm-parent"

include("runtime")
project(":runtime").name = "jupyter-jvm-runtime"

include("basekernel")
project(":basekernel").name = "jupyter-jvm-basekernel"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("jeromq", "org.zeromq:jeromq:0.5.1")
            library("gson", "com.google.code.gson:gson:2.10.1")
        }

        create("testLibs") {
            library("junit", "org.junit.jupiter:junit-jupiter:5.10.1")
            library("hamcrest", "org.hamcrest:hamcrest:2.2")
            library("jimfs", "com.google.jimfs:jimfs:1.3.0")
        }
    }
}

