package org.araqnid.reposerver.integration

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.describe
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.present
import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType

fun hasMimeType(mimeType: String): Matcher<HttpEntity> {
    return has(HttpEntity::parsedMimeType, present(Matcher("equals ${describe(mimeType)} (case-insensitively)") { it.equals(mimeType, ignoreCase = true)}))
}

val HttpEntity.parsedContentType: ContentType?
    get() = ContentType.get(this)

val HttpEntity.parsedMimeType: String?
    get() = parsedContentType?.mimeType
