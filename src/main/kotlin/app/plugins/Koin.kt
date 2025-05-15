package app.plugins

import auth.AuthService
import auth.repositories.AuthRepository
import auth.repositories.AuthRepositoryImpl
import configurations.gallery.GalleryService
import configurations.gallery.repositories.GalleryRepository
import configurations.gallery.repositories.GalleryRepositoryImpl
import configurations.gallery.repositories.SearchRepository
import configurations.gallery.repositories.SearchRepositoryImpl
import configurations.library.ConfigurationService
import configurations.library.SvgPreviewGenerator
import configurations.library.repositories.ConfigurationRepository
import configurations.library.repositories.ConfigurationRepositoryImpl
import configurations.util.PreviewGenerator

import io.ktor.server.application.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation  // <-- клиентский плагин
import io.ktor.serialization.kotlinx.json.json                     // <-- json() для клиента // <-- для Application
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.core.qualifier.named
import password_recovery.EmailService
import password_recovery.PasswordResetRepository
import password_recovery.PasswordResetService
import password_recovery.ResendEmailService
import users.UserService
import users.repositories.UserRepository
import users.repositories.UserRepositoryImpl
import java.io.File

//const val apiUrl = "localhost"
const val apiUrl = "https://flexypixelapi.fun"

fun Application.configureDi() {
    install(Koin) {
        modules(
            module {
                single<UserRepository> { UserRepositoryImpl() }
                single<AuthRepository> { AuthRepositoryImpl() }
                single { AuthService(get()) }
                single { File("uploads/avatars") }
                single(named("avatarBaseUrl")) { "${apiUrl}/uploads/avatars" }
                single {
                    SvgPreviewGenerator(
                        outputDir = File("previews"),
                        baseUrl = "${apiUrl}/previews"
                    )
                }
                single {
                    PreviewGenerator(
                        outputDir = File("previews"),
                        baseUrl = "${apiUrl}/previews"
                    )
                }
                single { UserService(get(), get(named("avatarBaseUrl")), get()) }
                single<ConfigurationRepository> { ConfigurationRepositoryImpl() }
                single { ConfigurationService(get(), get()) }
                single<GalleryRepository> { GalleryRepositoryImpl() }
                single<SearchRepository> { SearchRepositoryImpl() }
                single { GalleryService(get(), get(), get()) }

                single {
                    HttpClient(CIO) {
                        install(ContentNegotiation) {
                            json()
                        }
                    }
                }

                val apiKey = System.getenv("RESEND_API_KEY")
                    ?: error("RESEND_API_KEY is not set in environment")
                single<EmailService> {
                    ResendEmailService(
                        httpClient = get(),
                        apiKey = apiKey,
                        fromEmail = "no-reply@flexypixel.fun",
                    )
                }
                single { PasswordResetRepository() }

                single<PasswordResetService> {
                    PasswordResetService(
                        userRepository = get(),
                        passwordResetRepository = get(),
                        emailService = get(),
                        resetLinkBaseUrl = "https://flexypixel.fun/password-reset",
                        tokenExpiryMinutes = 15L
                    )
                }
            }
        )
    }
}