package app.plugins

import auth.repositories.AuthRepository
import auth.repositories.AuthRepositoryImpl
import configurations.library.repositories.ConfigurationRepository
import configurations.library.repositories.ConfigurationRepositoryImpl
import users.repositories.UserRepository
import users.repositories.UserRepositoryImpl
import configurations.library.ConfigurationService
import auth.AuthService
import users.UserService
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureDi() {
    install(Koin){
        modules(
            module {
                single<UserRepository> { UserRepositoryImpl() }
                single<AuthRepository>{ AuthRepositoryImpl() }
                single{ AuthService(get()) }
                single { UserService(get()) }
                single<ConfigurationRepository> { ConfigurationRepositoryImpl() }
                single { ConfigurationService(get()) }
            }
        )
    }
}