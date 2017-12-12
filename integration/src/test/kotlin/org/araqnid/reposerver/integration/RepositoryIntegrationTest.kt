package org.araqnid.reposerver.integration

import com.google.common.io.MoreFiles
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.containsSubstring
import com.natpryce.hamkrest.equalTo
import org.apache.http.HttpStatus.SC_BAD_REQUEST
import org.apache.http.HttpStatus.SC_CREATED
import org.apache.http.HttpStatus.SC_NOT_FOUND
import org.apache.http.HttpStatus.SC_OK
import org.apache.http.HttpStatus.SC_UNAUTHORIZED
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPut
import org.apache.http.entity.StringEntity
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path

class RepositoryIntegrationTest : IntegrationTest() {
    val storageDir: Path
        get() = server.temporaryFolder.root.toPath()

    @Test fun serves_maven_file_from_storage_directory() {
        val artifactPath = storageDir.resolve("com/example/project/0.0.0/project-0.0.0.txt")
        Files.createDirectories(artifactPath.parent)
        MoreFiles.asCharSink(artifactPath, UTF_8).write("test")

        execute(HttpGet("/maven/com/example/project/0.0.0/project-0.0.0.txt"))
        assertThat(response.statusLine.statusCode, equalTo(SC_OK))
        assertThat(response.entity.asCharSource(UTF_8).read(), equalTo("test"))
    }

    @Test fun serves_directory_listing_from_storage_directory() {
        val artifactPath = storageDir.resolve("com/example/project/0.0.0/project-0.0.0.txt")
        Files.createDirectories(artifactPath.parent)
        MoreFiles.asCharSink(artifactPath, UTF_8).write("test")

        execute(HttpGet("/maven/com/example/project/0.0.0"))
        assertThat(response.statusLine.statusCode, equalTo(SC_OK))
        assertThat(response.entity, hasMimeType("text/html"))
        assertThat(response.entity.asCharSource(UTF_8).read(), containsSubstring("project-0.0.0.txt"))
    }

    @Test fun returns_404_for_nonexistent_file() {
        execute(HttpGet("/maven/com/example/project/0.0.0/project-0.0.0.txt"))
        assertThat(response.statusLine.statusCode, equalTo(SC_NOT_FOUND))
    }

    @Test fun puts_file_into_storage_directory() {
        useBasicAuthentication("testuser", "testpassword")
        execute(HttpPut("/maven/com/example/project/0.0.0/project-0.0.0.txt").apply {
            entity = StringEntity("test", UTF_8)
        })
        assertThat(response.statusLine.statusCode, equalTo(SC_CREATED))
        val artifactPath = storageDir.resolve("com/example/project/0.0.0/project-0.0.0.txt")
        assertTrue("$artifactPath exists", Files.exists(artifactPath))
        assertThat(MoreFiles.asCharSource(artifactPath, UTF_8).read(), equalTo("test"))
    }

    @Test fun rejects_request_without_authentication() {
        execute(HttpPut("/maven/com/example/project/0.0.0/project-0.0.0.txt").apply {
            entity = StringEntity("test", UTF_8)
        })
        assertThat(response.statusLine.statusCode, equalTo(SC_UNAUTHORIZED))
        assertThat(response.getFirstHeader("WWW-Authenticate")?.value, equalTo("basic realm=\"Artifact repository\""))
    }

    @Test fun rejects_request_with_wrong_credential() {
        useBasicAuthentication("testuser", "bogus")
        execute(HttpPut("/maven/com/example/project/0.0.0/project-0.0.0.txt").apply {
            entity = StringEntity("test", UTF_8)
        })
        assertThat(response.statusLine.statusCode, equalTo(SC_UNAUTHORIZED))
    }

    @Test fun rejects_duplicate_file() {
        useBasicAuthentication("testuser", "testpassword")
        val artifactPath = storageDir.resolve("com/example/project/0.0.0/project-0.0.0.txt")
        Files.createDirectories(artifactPath.parent)
        MoreFiles.asCharSink(artifactPath, UTF_8).write("test")

        execute(HttpPut("/maven/com/example/project/0.0.0/project-0.0.0.txt").apply {
            entity = StringEntity("test", UTF_8)
        })
        assertThat(response.statusLine.statusCode, equalTo(SC_BAD_REQUEST))
    }

    @Test fun permits_overwrite_of_maven_metadata() {
        useBasicAuthentication("testuser", "testpassword")
        val artifactPath = storageDir.resolve("com/example/project/0.0.0/maven-metadata.xml")
        Files.createDirectories(artifactPath.parent)
        MoreFiles.asCharSink(artifactPath, UTF_8).write("test")

        execute(HttpPut("/maven/com/example/project/0.0.0/maven-metadata.xml").apply {
            entity = StringEntity("updated", UTF_8)
        })
        assertThat(response.statusLine.statusCode, equalTo(SC_CREATED))
        assertTrue("$artifactPath exists", Files.exists(artifactPath))
        assertThat(MoreFiles.asCharSource(artifactPath, UTF_8).read(), equalTo("updated"))
    }
}