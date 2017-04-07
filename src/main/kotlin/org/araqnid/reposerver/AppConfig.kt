package org.araqnid.reposerver

import com.google.common.util.concurrent.Service
import com.google.common.util.concurrent.ServiceManager
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.ProvisionException
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Names
import org.araqnid.appstatus.AppVersion
import org.araqnid.appstatus.ComponentsBuilder
import org.araqnid.appstatus.StatusComponent
import java.time.Clock
import javax.inject.Qualifier
import javax.inject.Singleton

class AppConfig(val environment: Map<String, String>) : AbstractModule() {
    override fun configure() {
        environment.forEach { k, v -> bindConstant().annotatedWith(Names.named(k)).to(v) }

        bind(Clock::class.java).toInstance(Clock.systemDefaultZone())

        with(Multibinder.newSetBinder(binder(), Service::class.java)) {
            addBinding().to(JettyService::class.java)
        }

        with(Multibinder.newSetBinder(binder(), Any::class.java, StatusSource::class.java)) {
            addBinding().toInstance(BasicStatusComponents)
        }
    }

    @Provides
    @Singleton
    fun serviceManager(services: @JvmSuppressWildcards Set<Service>) = ServiceManager(services)

    @Provides
    @Singleton
    fun appVersion() = AppVersion.fromPackageManifest(javaClass)

    @Provides
    @Singleton
    fun statusComponents(builder: ComponentsBuilder, @StatusSource statusComponentSources: @JvmSuppressWildcards Set<Any>): Collection<StatusComponent> {
        return builder.buildStatusComponents(*statusComponentSources.toTypedArray())
    }

    private fun getenv(key: String): String = environment[key] ?: throw ProvisionException("$key not specified in environment")
}

@Qualifier
annotation class StatusSource
