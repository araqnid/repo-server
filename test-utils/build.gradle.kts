plugins {
    kotlin("jvm")
}

val guavaVersion: String by rootProject.extra
val jacksonVersion: String by rootProject.extra

dependencies {
    compile(kotlin("test-junit", "1.2.10"))
    compile("com.natpryce:hamkrest:1.4.2.2")
    compile("org.araqnid:hamkrest-json:1.0.3")
    compile("com.google.guava:guava:$guavaVersion")
    compile("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    compile(kotlin("stdlib-jdk8", "1.2.10"))
}
