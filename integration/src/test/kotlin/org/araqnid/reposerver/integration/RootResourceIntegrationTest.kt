package org.araqnid.reposerver.integration

import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpGet
import org.araqnid.fuellog.matchers.jsonAnyString
import org.araqnid.fuellog.matchers.jsonNull
import org.araqnid.fuellog.matchers.jsonObject
import org.araqnid.fuellog.matchers.jsonTextStructuredAs
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Ignore
import org.junit.Test
import javax.ws.rs.core.MediaType

class RootResourceIntegrationTest : IntegrationTest() {

    @Ignore
    @Test fun root_has_json() {
        execute(HttpGet("/_api/").accepting(MediaType.APPLICATION_JSON))
        MatcherAssert.assertThat(response.statusLine.statusCode, Matchers.equalTo(HttpStatus.SC_OK))
        MatcherAssert.assertThat(response.entity, hasMimeType(MediaType.APPLICATION_JSON))
        MatcherAssert.assertThat(response.entity.text,
                jsonTextStructuredAs(jsonObject()
                        .withProperty("version", Matchers.either(jsonNull()).or(jsonAnyString()))
                        .withProperty("user_info", jsonNull())))
    }
}