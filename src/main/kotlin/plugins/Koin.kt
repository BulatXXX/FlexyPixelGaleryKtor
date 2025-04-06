package com.flexypixelgalleryapi.plugins

import com.flexypixelgalleryapi.repositories.UserRepository
import com.flexypixelgalleryapi.repositories.UserRepositoryImpl
import com.flexypixelgalleryapi.services.UserService
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureDi() {
    install(Koin){
        modules(
            module {
                single<UserRepository> {UserRepositoryImpl()}
                single{UserService(get())}
            }
        )
    }
}