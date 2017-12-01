plugins {
    kotlin("jvm")
}

configurations {
    "testRuntime" {
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
}

dependencies {
    testCompile(rootProject)
    testCompile(project(":test-utils"))
    testCompile("junit:junit:4.12")
    testCompile("org.hamcrest:hamcrest-library:1.3")
    testCompile("com.timgroup:clocks-testing:1.0.1070")
    testRuntime("org.slf4j:slf4j-simple:1.7.25")
}