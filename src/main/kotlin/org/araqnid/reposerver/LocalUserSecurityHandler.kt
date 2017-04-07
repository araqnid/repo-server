package org.araqnid.reposerver

import org.eclipse.jetty.security.RoleInfo
import org.eclipse.jetty.security.SecurityHandler
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.UserIdentity
import org.slf4j.LoggerFactory

class LocalUserSecurityHandler : SecurityHandler() {
    private val logger = LoggerFactory.getLogger(LocalUserSecurityHandler::class.java)

    override fun isAuthMandatory(baseRequest: Request, baseResponse: org.eclipse.jetty.server.Response, constraintInfo: Any): Boolean {
        logger.debug("isAuthMandatory(<req>, <resp>, constraintInfo: $constraintInfo)")
        return baseRequest.method == "PUT"
    }

    override fun checkUserDataPermissions(pathInContext: String, request: Request, response: org.eclipse.jetty.server.Response, constraintInfo: RoleInfo): Boolean {
        logger.debug("checkUserDataPermissions(pathInContext: $pathInContext, <req>, <resp>, constraintInfo: $constraintInfo)")
        return true
    }

    override fun prepareConstraintInfo(pathInContext: String, request: Request): RoleInfo {
        logger.debug("prepareConstraintInfo(pathInContext: $pathInContext, <req>)")
        return RoleInfo()
    }

    override fun checkWebResourcePermissions(pathInContext: String, request: Request, response: org.eclipse.jetty.server.Response, constraintInfo: Any, userIdentity: UserIdentity): Boolean {
        logger.debug("checkWebResourcePermissions(pathInContext: $pathInContext, <req>, <resp>, constraintInfo: $constraintInfo, userIdentity: $userIdentity)")
        return true // authorisation handled by app
    }
}
