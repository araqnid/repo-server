package org.araqnid.reposerver

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.araqnid.appstatus.AppVersion
import org.araqnid.appstatus.Readiness
import org.araqnid.appstatus.StatusComponent
import org.araqnid.appstatus.StatusPage
import javax.inject.Inject
import javax.inject.Singleton
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Singleton
class ReadinessServlet(val readiness: () -> Readiness) : HttpServlet() {
    constructor(value: Readiness) : this(readiness = { value })

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.contentType = "text/plain"
        resp.characterEncoding = "UTF-8"
        resp.writer.use { writer ->
            writer.print(readiness())
        }
    }
}

@Singleton
class StatusServlet @Inject constructor(val components: @JvmSuppressWildcards Collection<StatusComponent>) : HttpServlet() {
    val statusPageWriter: ObjectWriter = objectMapper.writerFor(StatusPage::class.java)

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val statusPage = StatusPage.build(components)
        resp.contentType = "application/json"
        resp.outputStream.use { output ->
            statusPageWriter.writeValue(output, statusPage)
        }
    }
}

@Singleton
class VersionServlet @Inject constructor(val appVersion: AppVersion) : HttpServlet() {
    val versionWriter: ObjectWriter = objectMapper.writerFor(AppVersion::class.java)

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.contentType = "application/json"
        resp.outputStream.use { output ->
            versionWriter.writeValue(output, appVersion)
        }
    }
}

val objectMapper: ObjectMapper = ObjectMapper()
        .registerModule(KotlinModule())
        .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
