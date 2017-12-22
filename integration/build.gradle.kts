plugins {
    kotlin("jvm")
}

configurations {
    "testRuntime" {
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
}

dependencies {
    testImplementation(rootProject)
    testImplementation(project(":test-utils"))
    testImplementation("com.timgroup:clocks-testing:1.0.1070")
    testImplementation("org.apache.httpcomponents:httpclient:4.5.4")
    testImplementation("org.jboss.spec.javax.ws.rs:jboss-jaxrs-api_2.0_spec:1.0.1.Beta1")
    testRuntimeOnly("org.slf4j:slf4j-simple:1.7.25")
}
