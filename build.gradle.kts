import org.araqnid.gradle.RuntimeDependenciesTask
import org.jetbrains.kotlin.daemon.common.toHexString
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

plugins {
    application
    kotlin("jvm") version "1.2.10"
}

application {
    mainClassName = "org.araqnid.reposerver.boot.Main"
}

val jettyVersion by extra { "9.4.7.v20170914" }
val jacksonVersion by extra { "2.9.2" }
val resteasyVersion by extra { "3.1.4.Final" }
val guiceVersion by extra { "4.1.0" }
val guavaVersion by extra { "23.0" }

val gitVersion by extra {
    val capture = ByteArrayOutputStream()
    project.exec {
        commandLine("git", "describe", "--tags")
        standardOutput = capture
    }
    String(capture.toByteArray())
            .trim()
            .removePrefix("v")
            .replace('-', '.')
}

allprojects {
    group = "org.araqnid"
    version = gitVersion

    tasks {
        withType<JavaCompile> {
            sourceCompatibility = "1.8"
            targetCompatibility = "1.8"
            options.encoding = "UTF-8"
            options.compilerArgs.add("-parameters")
            options.isIncremental = true
            options.isDeprecation = true
        }

        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    repositories {
        mavenCentral()
        maven(url = "https://repo.araqnid.org/maven/")
        maven(url = "https://dl.bintray.com/araqnid/maven")
    }
}

configurations {
    "runtime" {
        exclude(group = "ch.qos.logback", module = "jsr305")
    }
}

tasks {
    val runtimeDeps by creating(RuntimeDependenciesTask::class)

    "jar"(Jar::class) {
        manifest {
            attributes["Implementation-Title"] = project.description ?: project.name
            attributes["Implementation-Version"] = project.version
        }
        from(runtimeDeps) {
            into("META-INF")
        }
    }
}

dependencies {
    compile("org.araqnid:eventstore:0.0.20")
    compile("com.google.inject:guice:$guiceVersion")
    compile("org.eclipse.jetty:jetty-server:$jettyVersion")
    implementation(kotlin("stdlib-jdk8", "1.2.10"))
    implementation(kotlin("reflect", "1.2.10"))
    implementation("org.araqnid:app-status:0.0.11")
    implementation("com.google.guava:guava:$guavaVersion")
    implementation("com.google.inject.extensions:guice-servlet:$guiceVersion")
    implementation("com.google.inject.extensions:guice-multibindings:$guiceVersion")
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("org.eclipse.jetty:jetty-servlet:$jettyVersion")
    implementation("org.jboss.resteasy:resteasy-jaxrs:$resteasyVersion")
    implementation("org.jboss.resteasy:resteasy-guice:$resteasyVersion")
    implementation("com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-guice:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-guava:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("org.apache.httpcomponents:httpasyncclient:4.1.3")
    implementation("com.fasterxml.uuid:java-uuid-generator:3.1.3")
    implementation("org.tukaani:xz:1.5")
    implementation("org.apache.commons:commons-compress:1.13")
    testImplementation(project(":test-utils"))
    runtimeOnly("ch.qos.logback:logback-classic:1.2.2")
    runtimeOnly("org.slf4j:jcl-over-slf4j:1.7.25")
}
