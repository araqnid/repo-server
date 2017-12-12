import org.araqnid.gradle.RuntimeDependenciesTask
import org.jetbrains.kotlin.daemon.common.toHexString
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

plugins {
    application
    kotlin("jvm") version "1.2.0"
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
    create("boot")
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
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    compile("org.araqnid:app-status:0.0.11")
    compile("org.araqnid:eventstore:0.0.20")
    compile("com.google.guava:guava:$guavaVersion")
    compile("com.google.inject:guice:$guiceVersion")
    compile("com.google.inject.extensions:guice-servlet:$guiceVersion")
    compile("com.google.inject.extensions:guice-multibindings:$guiceVersion")
    compile("org.slf4j:slf4j-api:1.7.25")
    compile("org.eclipse.jetty:jetty-server:$jettyVersion")
    compile("org.eclipse.jetty:jetty-servlet:$jettyVersion")
    compile("org.jboss.resteasy:resteasy-jaxrs:$resteasyVersion")
    compile("org.jboss.resteasy:resteasy-guice:$resteasyVersion")
    compile("com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:$jacksonVersion")
    compile("com.fasterxml.jackson.module:jackson-module-guice:$jacksonVersion")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-guava:$jacksonVersion")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    compile("com.google.code.findbugs:jsr305:3.0.0")
    compile("org.apache.httpcomponents:httpasyncclient:4.1.3")
    compile("com.fasterxml.uuid:java-uuid-generator:3.1.3")
    compile("org.tukaani:xz:1.5")
    compile("org.apache.commons:commons-compress:1.13")
    testCompile(project(":test-utils"))
    runtime("ch.qos.logback:logback-classic:1.2.2")
    runtime("org.slf4j:jcl-over-slf4j:1.7.25")
}
