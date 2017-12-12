package org.araqnid.reposerver.integration

import com.natpryce.hamkrest.anything
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.or
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpGet
import org.araqnid.hamkrest.json.jsonBytes
import org.araqnid.hamkrest.json.jsonNull
import org.araqnid.hamkrest.json.jsonObject
import org.araqnid.hamkrest.json.jsonString
import org.junit.Test
import javax.ws.rs.core.MediaType

class InfoResourcesIntegrationTest : IntegrationTest() {
    @Test fun readiness_has_text() {
        execute(HttpGet(server.uri("/_api/info/readiness")).accepting(MediaType.TEXT_PLAIN))
        assertThat(response.statusLine.statusCode, equalTo(HttpStatus.SC_OK))
        assertThat(response.entity, hasMimeType(MediaType.TEXT_PLAIN))
        assertThat(response.entity.text, equalTo("READY"))
    }

    @Test fun version_has_json() {
        execute(HttpGet(server.uri("/_api/info/version")).accepting(MediaType.APPLICATION_JSON))
        assertThat(response.statusLine.statusCode, equalTo(HttpStatus.SC_OK))
        assertThat(response.entity, hasMimeType(MediaType.APPLICATION_JSON))
        assertThat(response.entity.bytes, jsonBytes(jsonObject()
                .withProperty("version", anything)
                .withProperty("title", jsonString(anything) or jsonNull())
                .withProperty("vendor", jsonNull())))
    }

    @Test fun status_has_json() {
        execute(HttpGet(server.uri("/_api/info/status")).accepting(MediaType.APPLICATION_JSON))
        assertThat(response.statusLine.statusCode, equalTo(HttpStatus.SC_OK))
        assertThat(response.entity, hasMimeType(MediaType.APPLICATION_JSON))
        assertThat(response.entity.bytes,
                jsonBytes(jsonObject()
                        .withProperty("status", jsonString(anything))
                        .withProperty("components", jsonObject()
                                .withPropertyJSON("jvmVersion", "{ priority: 'INFO', label: 'JVM version', text: '${System.getProperty("java.version")}' }")
                                .withAnyOtherProperties()
                        )))
    }
}
