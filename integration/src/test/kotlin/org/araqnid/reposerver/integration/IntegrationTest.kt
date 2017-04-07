package org.araqnid.reposerver.integration

import com.google.common.io.ByteStreams
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.HttpVersion
import org.apache.http.auth.AuthScope
import org.apache.http.auth.Credentials
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.message.BasicHttpResponse
import org.apache.http.message.BasicNameValuePair
import org.junit.Rule

abstract class IntegrationTest {
    @Rule @JvmField val server = ServerRunner(mapOf("PORT" to "0", "DOCUMENT_ROOT" to "../ui/www"))

    var response: HttpResponse = BasicHttpResponse(HttpVersion.HTTP_1_0, HttpStatus.SC_INTERNAL_SERVER_ERROR, "No request executed")
    val httpContext = HttpClientContext()

    fun execute(request: HttpUriRequest) {
        response = server.client.execute(request, { rawResponse ->
            val bufferedResponse = BasicHttpResponse(rawResponse.statusLine)
            rawResponse.allHeaders.forEach { bufferedResponse.addHeader(it) }
            if (rawResponse.entity != null) {
                val bytes = ByteStreams.toByteArray(rawResponse.entity.content)
                bufferedResponse.entity = ByteArrayEntity(bytes).apply {
                    contentType = rawResponse.entity.contentType
                }
            }
            bufferedResponse
        }, httpContext)
    }

    fun useBasicAuthentication(username: String, password: String) {
        httpContext.credentialsProvider = object : CredentialsProvider {
            override fun getCredentials(authscope: AuthScope): Credentials {
                return UsernamePasswordCredentials(username, password)
            }

            override fun setCredentials(authscope: AuthScope, credentials: Credentials?) {
            }

            override fun clear() {
            }
        }
    }
}

internal fun formEntity(params: Map<String, String>) = UrlEncodedFormEntity(params.map { (k, v) -> BasicNameValuePair(k, v) }.toList())
