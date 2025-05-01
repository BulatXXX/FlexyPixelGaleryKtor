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
import configurations.util.PreviewGenerator
import configurations.library.SvgPreviewGenerator
import users.UserService
import io.ktor.server.application.*
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.File

//const val apiUrl = "localhost"
const val apiUrl = "91.200.13.57"

fun Application.configureDi() {
    install(Koin) {
        modules(
            module {
                single<UserRepository> { UserRepositoryImpl() }
                single<AuthRepository> { AuthRepositoryImpl() }
                single { AuthService(get()) }
                single { File("uploads/avatars") }
                single(named("avatarBaseUrl")) { "http://${apiUrl}:8080/uploads/avatars" }
                single {
                    SvgPreviewGenerator(
                        outputDir = File("previews"),
                        baseUrl = "http://localhost:8080/previews"
                    )
                }
                single {
                    PreviewGenerator(
                        outputDir = File("previews"),
                        baseUrl = "http://localhost:8080/previews"
                    )
                }
                single { UserService(get(), get(named("avatarBaseUrl")), get()) }
                single<ConfigurationRepository> { ConfigurationRepositoryImpl() }
                single { ConfigurationService(get(), get()) }
                single<GalleryRepository> { GalleryRepositoryImpl() }
                single<SearchRepository> { SearchRepositoryImpl() }
                single { GalleryService(get(), get()) }
            }
        )
    }
}