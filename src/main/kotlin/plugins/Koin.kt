package com.flexypixelgalleryapi.plugins

import com.flexypixelgalleryapi.repositories.ConfigurationRepository
import com.flexypixelgalleryapi.repositories.ConfigurationRepositoryImpl
import com.flexypixelgalleryapi.repositories.UserRepository
import com.flexypixelgalleryapi.repositories.UserRepositoryImpl
import com.flexypixelgalleryapi.services.ConfigurationService
import com.flexypixelgalleryapi.services.AuthService
import com.flexypixelgalleryapi.services.UserService
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureDi() {
    install(Koin){
        modules(
            module {
                single<UserRepository> {UserRepositoryImpl()}
                single{AuthService(get())}
                single { UserService(get()) }
                single<ConfigurationRepository> {ConfigurationRepositoryImpl()}
                single { ConfigurationService(get()) }
            }
        )
    }
}