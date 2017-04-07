package org.araqnid.reposerver.integration

import com.fasterxml.jackson.databind.JsonNode
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpGet
import org.araqnid.fuellog.matchers.jsonAnyString
import org.araqnid.fuellog.matchers.jsonBytesStructuredAs
import org.araqnid.fuellog.matchers.jsonNull
import org.araqnid.fuellog.matchers.jsonObject
import org.araqnid.fuellog.matchers.jsonString
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Test
import javax.ws.rs.core.MediaType

class InfoResourcesIntegrationTest : IntegrationTest() {
    @Test fun readiness_has_text() {
        execute(HttpGet(server.uri("/_api/info/readiness")).accepting(MediaType.TEXT_PLAIN))
        assertThat(response.statusLine.statusCode, Matchers.equalTo(HttpStatus.SC_OK))
        assertThat(response.entity, hasMimeType(MediaType.TEXT_PLAIN))
        assertThat(response.entity.text, Matchers.equalTo("READY"))
    }

    @Test fun version_has_json() {
        execute(HttpGet(server.uri("/_api/info/version")).accepting(MediaType.APPLICATION_JSON))
        assertThat(response.statusLine.statusCode, Matchers.equalTo(HttpStatus.SC_OK))
        assertThat(response.entity, hasMimeType(MediaType.APPLICATION_JSON))
        assertThat(response.entity.bytes, jsonBytesStructuredAs(jsonObject()
                .withProperty("version", Matchers.any(JsonNode::class.java))
                .withProperty("title", jsonAnyString())
                .withProperty("vendor", jsonNull())))
    }

    @Test fun status_has_json() {
        execute(HttpGet(server.uri("/_api/info/status")).accepting(MediaType.APPLICATION_JSON))
        assertThat(response.statusLine.statusCode, Matchers.equalTo(HttpStatus.SC_OK))
        assertThat(response.entity, hasMimeType(MediaType.APPLICATION_JSON))
        assertThat(response.entity.bytes,
                jsonBytesStructuredAs(jsonObject()
                        .withProperty("status", jsonString(Matchers.any(String::class.java)))
                        .withProperty("components", jsonObject()
                                .withPropertyJSON("jvmVersion", "{ priority: 'INFO', label: 'JVM version', text: '${System.getProperty("java.version")}' }")
                                .withAnyOtherProperties()
                        )))
    }
}
