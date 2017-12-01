plugins {
    kotlin("jvm")
}

configurations {
    "runtime" {
        exclude(group = "ch.qos.logback", module = "jsr305")
    }
}

dependencies {
    compile(rootProject)
}
