plugins {
    kotlin("jvm")
}

val guavaVersion: String by rootProject.extra
val jacksonVersion: String by rootProject.extra

dependencies {
    compile(kotlin("test-junit"))
    compile("com.natpryce:hamkrest:1.4.2.2")
    compile("org.araqnid:hamkrest-json:1.0.3")
    compile("com.google.guava:guava:$guavaVersion")
    compile("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}
