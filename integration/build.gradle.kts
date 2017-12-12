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
    testCompile("com.timgroup:clocks-testing:1.0.1070")
    testRuntime("org.slf4j:slf4j-simple:1.7.25")
}
