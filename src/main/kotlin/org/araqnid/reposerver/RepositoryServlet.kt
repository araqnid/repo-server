package org.araqnid.reposerver

import com.google.common.io.MoreFiles
import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator
import java.util.Comparator.comparingInt
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST
import javax.servlet.http.HttpServletResponse.SC_CREATED
import javax.servlet.http.HttpServletResponse.SC_NOT_FOUND

class RepositoryServlet (val repositoryDir: Path) : HttpServlet() {

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val pathname = req.pathInfo
        val path = repositoryDir.resolve(if (pathname.startsWith("/")) pathname.substring(1) else pathname)
        if (!Files.exists(path)) {
            resp.sendError(SC_NOT_FOUND)
            return
        }
        if (Files.isDirectory(path)) {
            resp.contentType = "text/html"
            resp.writer.use { writer ->
                writer.println("<title>$path</title>")
                writer.println("<ul>")
                Files.list(path)
                        .map { FileInDirectory(name = it.fileName.toString(), isSubdirectory = Files.isDirectory(it)) }
                        .sorted(FileInDirectory.fileListingComparator)
                        .forEachOrderedAndClose { (name, isSubdirectory) ->
                            if (isSubdirectory)
                                writer.println("<li><a href=\"$name/\">$name</a> /</li>")
                            else
                                writer.println("<li><a href=\"$name\">$name</a></li>")
                        }
                writer.println("</ul>")
            }
        }
        else {
            MoreFiles.asByteSource(path).copyTo(resp.outputStream)
        }
    }

    override fun doPut(req: HttpServletRequest, resp: HttpServletResponse) {
        val pathname = req.pathInfo
        val path = repositoryDir.resolve(if (pathname.startsWith("/")) pathname.substring(1) else pathname)
        if (Files.exists(path) && !path.fileName.toString().startsWith("maven-metadata.xml")) {
            resp.sendError(SC_BAD_REQUEST, "Already exists")
            return
        }
        Files.createDirectories(path.parent)
        MoreFiles.asByteSink(path).writeFrom(req.inputStream)
        resp.status = SC_CREATED
        resp.addHeader("Location", req.requestURI)
    }

    private data class FileInDirectory(val name: String, val isSubdirectory: Boolean) {
        companion object {
            val fileListingComparator: Comparator<FileInDirectory> = comparingInt { f: FileInDirectory -> if (f.isSubdirectory) 0 else 1 }
                    .thenComparing { f -> f.name }

        }
    }
}
