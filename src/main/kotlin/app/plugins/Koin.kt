package app.plugins

import auth.repositories.AuthRepository
import auth.repositories.AuthRepositoryImpl
import configurations.library.repositories.ConfigurationRepository
import configurations.library.repositories.ConfigurationRepositoryImpl
import users.repositories.UserRepository
import users.repositories.UserRepositoryImpl
import configurations.library.ConfigurationService
import auth.AuthService
import configurations.gallery.GalleryService
import configurations.gallery.repositories.GalleryRepository
import configurations.gallery.repositories.GalleryRepositoryImpl
import configurations.gallery.repositories.SearchRepository
import configurations.gallery.repositories.SearchRepositoryImpl
import users.UserService
import io.ktor.server.application.*
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.File

fun Application.configureDi() {
    install(Koin) {
        modules(
            module {
                single<UserRepository> { UserRepositoryImpl() }
                single<AuthRepository> { AuthRepositoryImpl() }
                single { AuthService(get()) }
                single { File("uploads/avatars") }
                single(named("avatarBaseUrl")) { "http://91.200.13.57:8080/uploads/avatars" }
                single { UserService(get(), get(named("avatarBaseUrl")), get()) }
                single<ConfigurationRepository> { ConfigurationRepositoryImpl() }
                single { ConfigurationService(get()) }
                single<GalleryRepository> { GalleryRepositoryImpl() }
                single<SearchRepository> { SearchRepositoryImpl() }
                single { GalleryService(get(), get()) }
            }
        )
    }
}