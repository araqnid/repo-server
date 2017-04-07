package org.araqnid.reposerver

import com.google.common.util.concurrent.AbstractIdleService
import org.eclipse.jetty.security.LoginService
import org.eclipse.jetty.security.authentication.BasicAuthenticator
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.Slf4jRequestLog
import org.eclipse.jetty.server.handler.HandlerWrapper
import org.eclipse.jetty.server.handler.StatisticsHandler
import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.util.thread.QueuedThreadPool
import java.nio.file.Paths
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class JettyService @Inject constructor(@Named("PORT") port: Int,
                                       @Named("ARTIFACT_STORAGE") artifactStorage: String,
                                       loginService: LoginService,
                                       versionServlet: VersionServlet,
                                       statusServlet: StatusServlet,
                                       readinessServlet: ReadinessServlet) : AbstractIdleService() {
    val threadPool = QueuedThreadPool().apply {
        name = "Jetty"
    }
    val server: Server = Server(threadPool).apply {
        addConnector(ServerConnector(this, 1, 1).apply {
            this.port = port
        })
        requestLog = Slf4jRequestLog().apply {
            logLatency = true
        }
        val servletContext = ServletContextHandler().apply {
            val repositoryBaseDir = Paths.get(artifactStorage)
            resourceBase = artifactStorage
            securityHandler = LocalUserSecurityHandler()
            securityHandler.authenticator = BasicAuthenticator()
            securityHandler.loginService = loginService
            addServlet(ServletHolder(readinessServlet), "/_api/info/readiness")
            addServlet(ServletHolder(versionServlet), "/_api/info/version")
            addServlet(ServletHolder(statusServlet), "/_api/info/status")
            addServlet(ServletHolder(RepositoryServlet(repositoryBaseDir)), "/maven/*")
            addServlet(ServletHolder(RepositoryServlet(repositoryBaseDir)), "/ivy/*")
        }
        handler = GzipHandler() wrapping StatisticsHandler() wrapping servletContext
    }

    override fun startUp() {
        server.start()
    }

    override fun shutDown() {
        server.stop()
    }

    private infix fun <T : HandlerWrapper> T.wrapping(handler: Handler): T {
        this.handler = handler
        return this
    }

    private fun <First : HandlerWrapper, Next: Handler> First.andThen(nextHandler: Next): Next {
        handler = nextHandler
        return nextHandler
    }

    companion object {

    }
}